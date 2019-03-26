package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class ActualContractMainMunicipalServices extends View {
    
    public static final String TABLE_NAME = "vw_ca_municipal_svc";

    public enum c implements ColEnum {
        
        ID                   (Type.UUID,     "id"),
	FIASHOUSEGUID        (VocBuilding.class,   null,   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_M_M_SERVICE     (MainMunicipalService.class, null, "Коммунальная услуга"),
        STARTDATE            (Type.STRING,   "Начало"),
        ENDDATE              (Type.STRING,   "Окончание"),
        ;        

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }
    

    public ActualContractMainMunicipalServices () {        
        
        super  (TABLE_NAME, "Коммунальные услуги, оказываемые по адресам в рамках действующих договоров управления");

        cols   (c.class);
        pk     (c.ID);        

    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            "  ms.uuid id" +
            "  , co.fiashouseguid" +
            "  , ms.uuid uuid_m_m_service" +
            "  , cs.startdate" +
            "  , cs.enddate " +
            "FROM" +
            "  vw_contract_objects co " +
            "  INNER JOIN tb_contract_services cs ON (cs.uuid_contract_object=co.uuid AND cs.is_deleted=0 AND cs.is_additional=0) " +
            "  INNER JOIN tb_municipal_svc     ms ON (ms.is_deleted=0 AND ms.uuid_org = co.uuid_org AND ms.code_vc_nsi_3 = cs.code_vc_nsi_3) " +
            "WHERE" +
            "  co.is_own=1";

    }

}