package ru.eludia.products.mosgis.db.model.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType.Pair;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType.PlannedVolume;

public class SupplyResourceContractObject extends EnTable {

    public static final String TABLE_NAME = "tb_sr_ctr_obj";

    public enum c implements EnColEnum {
        
	UUID_XL               (InXlFile.class, "Файл импорта"),

	UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),

        ID_CTR_STATUS         (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус объекта жилищного фонда с точки зрения mosgis"),
	OBJECTGUID            (Type.UUID, null, "Идентификатор ОЖФ в ГИС ЖКХ"),

        FIASHOUSEGUID         (VocBuilding.class, "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_PREMISE          (Premise.class, null, "Помещение"),

        ID_LOG                (SupplyResourceContractObjectLog.class, null, "Последнее событие редактирования")
        ;

        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

        @Override
        public boolean isLoggable() {
            switch (this) {
                case UUID_SR_CTR:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

	public boolean isToXlImport() {

	    switch (this) {
		case UUID_XL:
		case FIASHOUSEGUID:
		case UUID_PREMISE:
		    return true;
	    default:
		return false;
	    }

	}
    }

    public static Map<String, Object> toHASH(ExportSupplyResourceContractObjectAddressResultType obj) {

	final Map<String, Object> r = DB.to.Map (obj);

	r.put (EnTable.c.UUID.lc(), obj.getObjectGUID());

	r.put (c.ID_CTR_STATUS.lc(), VocGisStatus.i.APPROVED.getId());

	final Map<String, Map<String, Object>> transportguid2subj = new HashMap();

	for (Pair pair : obj.getPair()) {

	    Map<String, Object> subj = DB.to.Map(pair);

	    subj.put(EnTable.c.UUID.lc(), pair.getTransportGUID());
	    subj.put(SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc(), pair.getServiceType().getCode());
	    subj.put(SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc(), pair.getMunicipalResource().getCode());

	    final List<Pair.HeatingSystemType> hss = pair.getHeatingSystemType();

	    if (hss != null && !hss.isEmpty()) {
		Pair.HeatingSystemType hs = hss.get(0);
		subj.put(SupplyResourceContractSubject.c.IS_HEAT_OPEN.lc(), DB.eq(hs.getOpenOrNot(), "Opened")? 1 : 0);
		subj.put(SupplyResourceContractSubject.c.IS_HEAT_CENTRALIZED.lc(), DB.eq(hs.getCentralizedOrNot(), "Centralized") ? 1 : 0);
	    }

	    transportguid2subj.put(DB.to.String(subj.get("transportguid")), subj);
	}

	for (PlannedVolume vol : obj.getPlannedVolume()) {

	    final Map<String, Object> v = DB.to.Map(vol);
	    final Map<String, Object> subj = transportguid2subj.get(vol.getPairKey());

	    if (DB.ok (subj)) {
		subj.put(SupplyResourceContractSubject.c.VOLUME.lc(), v.get("volume"));
		subj.put(SupplyResourceContractSubject.c.UNIT.lc(), v.get("unit"));
		subj.put(SupplyResourceContractSubject.c.FEEDINGMODE.lc(), v.get("feedingmode"));
	    }

	}

	return r;
    }

    public SupplyResourceContractObject () {

        super (TABLE_NAME, "Объект жилищного фонда договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);

        key   ("fiashouseguid1", c.FIASHOUSEGUID, c.UUID_PREMISE);
	
	key   ("objectguid", c.OBJECTGUID);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + " cnt NUMBER; "
	    + " hex VARCHAR2 (32); "
	    + "BEGIN "
	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
                
                + "SELECT COUNT(*) INTO cnt FROM " + VocBuilding.TABLE_NAME + " WHERE :NEW.fiashouseguid = houseguid;"
                + "IF cnt=0 THEN BEGIN "
                    + "hex := RAWTOHEX (:NEW.fiashouseguid); "
                    + "IF REGEXP_LIKE (hex, '^\\d+$') THEN BEGIN "
                        + "SELECT COUNT(*), MIN (fiashouseguid) INTO cnt, :NEW.fiashouseguid FROM " + VocUnom.TABLE_NAME + " WHERE 0+hex = unom;"
                        + "IF cnt>1 THEN raise_application_error (-20000, 'Не удалось однозначно определить FiasHouseGUID по UNOM'); END IF;"
                    + "END; END IF;"
                    + "IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение FiasHouseGUID (UNOM)'); END IF;"                                
                + "END; END IF;"
                
		+ " FOR i IN ("
		    + "SELECT "
		    + " o.uuid_premise     uuid_premise"
		    + " , premise.label    premise_label"
		    + " , building.label   building_label "
		    + " , sr_ctr.label     sr_ctr_label "
		    + "FROM "
		    + " tb_sr_ctr_obj o "
		    + " INNER JOIN tb_sr_ctr    sr_ctr   ON o.uuid_sr_ctr = sr_ctr.uuid "
		    + " INNER JOIN vc_buildings building ON o.fiashouseguid = building.houseguid "
		    + " LEFT JOIN vw_premises  premise  ON o.uuid_premise = premise.id "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
		    + " AND o.fiashouseguid   = :NEW.fiashouseguid "
		    + " AND NVL(o.uuid_premise, '00') = NVL(:NEW.uuid_premise, '00') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Объект жилищного фонда ' || i.building_label "
		    + "|| CASE WHEN i.uuid_premise IS NULL THEN '' ELSE  (' помещение ' || i.premise_label) END "
		    + "|| ' уже есть в договоре ' || i.sr_ctr_label "
		    + "|| '. Операция отменена.'); "
		+ " END LOOP; "

//		+ " IF :NEW.uuid_premise IS NULL THEN BEGIN "
//		    + " FOR sr_ctr IN (SELECT id_customer_type FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr "
//		    + ") LOOP"
//		    + "   FOR h IN (SELECT DISTINCT h.is_condo FROM vw_premises p LEFT JOIN tb_houses h ON p.uuid_house = h.uuid WHERE h.fiashouseguid = :NEW.fiashouseguid "
//		    + "   ) LOOP"
//
//			+ " IF h.is_condo = 1 AND sr_ctr.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.OWNER.getId()
//			+ " THEN "
//			+ "     raise_application_error (-20000, 'Укажите помещение МКД. Операция отменена.'); "
//			+ " END IF; "
//
//			+ " IF h.is_condo = 0 AND sr_ctr.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER.getId()
//			+ " THEN "
//			+ "     raise_application_error (-20000, 'Укажите помещение ЖД. Операция отменена.'); "
//			+ " END IF; "
//
//		    + "   END LOOP; "
//		    + " END LOOP; "
//		+ " END; END IF; " // IF :NEW.uuid_premise IS NULL

	    + " END; END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

}
