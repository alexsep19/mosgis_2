package ru.eludia.products.mosgis.db.model.voc;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;

public class VocPerson extends EnTable {

    public static final String TABLE_NAME = "vc_persons";

    public enum c implements EnColEnum {
        UUID_XL        (InXlFile.class, null, "Источник импорта"),

        UUID_ORG       (VocOrganization.class,          "Поставщик информации"),
        
        IS_FEMALE      (Type.BOOLEAN,       null,       "Пол"),
        BIRTHDATE      (Type.DATE,          null,       "Дата рождения"),
        
        SNILS          (Type.NUMERIC, 11,   null,       "СНИЛС"),
        
        CODE_VC_NSI_95 (Type.STRING,  20,   null,      "Код документа, удостоверяющего личность (НСИ 95)"),
        SERIES         (Type.STRING,  45,   null,      "Серия документа, удостоверяющего дичность"),
        NUMBER_        (Type.STRING,  45,   null,      "Номер документа, удостоверяющего личность"),
        ISSUEDATE      (Type.DATE,          null,      "Дата выдачи документа, удостоверяющего личность"),
        ISSUER         (Type.STRING,        null,      "Кем выдан документ, удостоверяющий личность"),
        
        PLACEBIRTH     (Type.STRING,  255,  null,      "Место рождения"),
        
        SURNAME        (Type.STRING,  256,             "Фамилия"),
        FIRSTNAME      (Type.STRING,  256,             "Имя"),
        PATRONYMIC     (Type.STRING,  256,  null,      "Отчество"),
        
        LABEL          (Type.STRING,     new Virt("DECODE(\"PATRONYMIC\", NULL, (\"SURNAME\" || ' ' || \"FIRSTNAME\"), (\"SURNAME\" || ' ' || \"FIRSTNAME\" || ' ' || \"PATRONYMIC\"))"), "ФИО"),
        LABEL_UC       (Type.STRING,     new Virt("UPPER(\"LABEL\")"), "ФИО (в верхнем регистре)"),
        
        SEX            (Type.STRING,  1, new Virt("DECODE(IS_FEMALE, 1, 'F', 0, 'M', NULL)"), "Пол (ГИС)"),
        
        ID_LOG         (VocPersonLog.class, null,      "Последнее событие редактирования"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

	@Override
	public boolean isLoggable() {
	    switch (this) {
	    case ID_LOG:
	    case UUID_ORG:
	    case LABEL:
	    case LABEL_UC:
		return false;
	    default:
		return true;
	    }
	}

	public boolean isToXlImport() {

	    switch (this) {
	    case UUID_XL:
	    case SURNAME:
	    case FIRSTNAME:
	    case PATRONYMIC:
	    case IS_FEMALE:
	    case BIRTHDATE:
	    case SNILS:
	    case CODE_VC_NSI_95:
	    case SERIES:
	    case NUMBER_:
	    case ISSUEDATE:
		return true;
	    default:
		return false;
	    }

	}
    }

    public VocPerson () {
        
        super (TABLE_NAME, "Физические лица");
        
        cols(c.class);

        key ("uuid_org", "uuid_org");

        trigger("BEFORE INSERT OR UPDATE", ""
            + "DECLARE "
            + " cnt INTEGER := 0;"
            + "BEGIN "

            + "IF :OLD.is_deleted=0 AND :NEW.is_deleted=1 THEN BEGIN "

            + " SELECT COUNT(*) INTO cnt FROM tb_accounts WHERE is_deleted=0 AND id_ctr_status <> " + VocGisStatus.i.ANNUL + "  AND uuid_person_customer=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в лицевых счетах.'); END IF; "

            + " SELECT COUNT(*) INTO cnt FROM tb_cit_comps WHERE is_deleted=0 AND id_ctr_status <> " + VocGisStatus.i.ANNUL + "  AND uuid_person=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в записях о гражданах, получающих компенсацию расходов.'); END IF; "
    
            + " SELECT COUNT(*) INTO cnt FROM tb_org_member_docs WHERE is_deleted=0 AND uuid_person_member=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в документах о членах товарищества, кооператива.'); END IF; "

            + " SELECT COUNT(*) INTO cnt FROM tb_prop_docs WHERE is_deleted=0 AND uuid_person_owner=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в документах о правах собственности.'); END IF; "

            + " SELECT COUNT(*) INTO cnt FROM tb_pp_ctr WHERE is_deleted=0 AND id_ctr_status <> " + VocGisStatus.i.ANNUL + "  AND uuid_person_customer=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в договорах на пользование общим имуществом.'); END IF; "
    
            + " SELECT COUNT(*) INTO cnt FROM tb_sr_ctr WHERE is_deleted=0 AND id_ctr_status <> " + VocGisStatus.i.ANNUL + "  AND uuid_person_customer=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в договорах ресурсоснабженияв.'); END IF; "

            + "END; END IF;"
        + "END;");
    }
    
    public static String getCustomerUuidBySnils (Map<String, Object> r, String snils) throws Exception {
        
        r.put (VocPerson.c.SNILS.lc (), snils);

        final MosGisModel m = ModelHolder.getModel ();

        try (DB db = m.getDb ()) {

            db.upsert (VocPerson.class, r, VocPerson.c.SNILS.lc ());

            return  db.getString (m
                .select (VocPerson.class, EnTable.c.UUID.lc ())
                .where  (c.UUID_ORG, r.get (c.UUID_ORG.lc ()))
                .where  (VocPerson.c.SNILS, snils)
            );

        }
        
    }    
    
    public static String getCustomerUuidByLegalId (Map<String, Object> r, ID id) throws Exception {
        
        if (id == null) throw new IllegalArgumentException ("Не указан ни СНИЛС, ни документ");

        if (!DB.ok (id.getSeries ())) throw new IllegalArgumentException ("Не указана серия документа");
        r.put (c.SERIES.lc (), id.getSeries ());
        r.put (c.NUMBER_.lc (), id.getNumber ());
        r.put (c.ISSUEDATE.lc (), id.getIssueDate ());
        r.put (c.CODE_VC_NSI_95.lc (), id.getType ().getCode ());

        final MosGisModel m = ModelHolder.getModel ();

        try (DB db = m.getDb ()) {

            db.upsert (VocPerson.class, r
                , c.SERIES.lc ()
                , c.NUMBER_.lc ()
                , c.ISSUEDATE.lc ()
                , c.CODE_VC_NSI_95.lc ()
            );

            return db.getString (m
                .select (VocPerson.class, EnTable.c.UUID.lc ())
                .where  (c.UUID_ORG, r.get (c.UUID_ORG.lc ()))
                .where  (c.SERIES, id.getSeries ())
                .where  (c.NUMBER_, id.getNumber ())
                .where  (c.ISSUEDATE, id.getIssueDate ())
                .where  (c.CODE_VC_NSI_95, id.getType ().getCode ())                
            );

        }
        
    }
        
}
