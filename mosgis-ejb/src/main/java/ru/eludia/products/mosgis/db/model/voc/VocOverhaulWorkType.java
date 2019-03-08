package ru.eludia.products.mosgis.db.model.voc;

import java.util.Map;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFiasAddressRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementNsiRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementStringFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;

public class VocOverhaulWorkType extends EnTable {
    
    private static final Logger logger = Logger.getLogger (VocOverhaulWorkType.class.getName ());
    
    public enum c implements EnColEnum {
        
        GUID            (Type.UUID, null,        "Глобально-уникальный идентификатор элемента справочника"),
        
        UUID_ORG        (VocOrganization.class,  "Организация"),
        
        CODE            (Type.STRING,  20, null, "Код элемента справочника, уникальный в пределах справочника"),
        SERVICENAME     (Type.STRING, 500,       "Наименование вида работ"),
        ISACTUAL        (Type.BOOLEAN,     null, "Признак актуальности элемента справочника"),
        CODE_VC_NSI_218 (Type.STRING,  20,       "Группа работ (НСИ 218)"),
        
        ID_STATUS       (VocAsyncEntityState.class, new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации"),
        
        ID_LOG          (VocOverhaulWorkTypeLog.class, "Последнее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;                    
            }

        }
        
    }
    
    public VocOverhaulWorkType () {
        
        super ("vc_oh_wk_types", "Справочник типов работ капитального ремонта");
        
        cols  (c.class);
        
    }
    
    public static Map<String, Object> toHASH (NsiElementType t) {
        
        final Map<String, Object> result = DB.HASH (
            "isactual",   t.isIsActual () ? 1 : 0,
            "code", t.getCode (),
            "guid",  t.getGUID ()
        );

        for (NsiElementFieldType f: t.getNsiElementField ()) {
            
            if (f instanceof NsiElementStringFieldType) {
                result.put ("servicename", ((NsiElementStringFieldType) f).getValue ());
            }
            else if (f instanceof NsiElementNsiRefFieldType) {
                result.put ("code_vc_nsi_218", ((NsiElementNsiRefFieldType) f).getNsiRef ().getRef ().getCode ());
            }
            else if (f instanceof NsiElementFiasAddressRefFieldType) {
                result.put ("fias_address_guid", ((NsiElementFiasAddressRefFieldType) f).getNsiRef ().getGuid ());
                result.put ("fias_address_aoguid", ((NsiElementFiasAddressRefFieldType) f).getNsiRef ().getAoGuid ());
            }
            
        }
        
        logger.info ("IMPORT RESULT: " + result.toString ());

        return result;
        
    }
    
    public enum Action {

	PLACING (VocGisStatus.i.PENDING_RP_PLACING, VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
	EDITING (VocGisStatus.i.PENDING_RP_EDIT, VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),;

	VocGisStatus.i nextStatus;
	VocGisStatus.i okStatus;
	VocGisStatus.i failStatus;

	private Action(VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
	    this.nextStatus = nextStatus;
	    this.okStatus = okStatus;
	    this.failStatus = failStatus;
	}

	public VocGisStatus.i getNextStatus() {
	    return nextStatus;
	}

	public VocGisStatus.i getFailStatus() {
	    return failStatus;
	}

	public VocGisStatus.i getOkStatus() {
	    return okStatus;
	}

	public static Action forStatus(VocGisStatus.i status) {
	    switch (status) {
	    case PENDING_RQ_PLACING:
		return PLACING;
	    case PENDING_RQ_EDIT:
		return EDITING;
	    case PENDING_RP_PLACING:
		return PLACING;
	    case PENDING_RP_EDIT:
		return EDITING;
	    default:
		return null;
	    }
	}
    };
    
}
