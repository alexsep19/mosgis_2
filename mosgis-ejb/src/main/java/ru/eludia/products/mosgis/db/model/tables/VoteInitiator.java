package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class VoteInitiator extends EnTable {
    
    public enum c implements EnColEnum {
        
        UUID_ORG      (VocOrganization.class,            "Организация"    ),
        UUID_IND      (PropertyDocument.class,           "Физическое лицо"),
        UUID_PROTOCOL (VotingProtocol.class,             "Протокол"),
        
        ID_LOG     (VoteInitiatorLog.class,     "Последнее событие редактирования"),
        
        IND_SURNAME        (STRING, 256, null, "Фамилия"),
        IND_FIRSTNAME      (STRING, 256, null, "Имя"),
        IND_PATRONYMIC     (STRING, 256, null, "Отчество"),
        IND_ISFEMALE       (BOOLEAN,     null, "Пол (1 - женский, 0 - мужской)"),
        IND_CODE_VC_NSI_95 (STRING, 20,  null, "Код документа, удостоверяющего личность (НСИ 95)"),
        IND_SERIES         (STRING, 45,  null, "Серия документа, удостоверяющего личность"),
        IND_NUMBER         (STRING, 45,  null, "Номер документа, удостоверяющего личность"),
        IND_ISSUE_DATE     (DATE,        null, "Дата выдачи документа, удостоверяющего личность"),
        IND_ISSUER         (STRING,      null, "Кем выдан документ, удостоверяющий личность"),
        
        IND_LABEL          (STRING, new Virt ("\"IND_SURNAME\"||' '||\"IND_FIRSTNAME\"||' '||\"IND_PATRONYMIC\""), "ФИО физического лица"),
        
        ORG_OGRN           (NUMERIC, 15, null, "ОГРН"),
        ORG_INN            (NUMERIC, 12, null, "ИНН"),
        ORG_KPP            (NUMERIC,  9, null, "КПП"),
        ORG_OKOPF          (NUMERIC,  5, null, "ОКОПФ"),
        
        ORG_LABEL          (STRING,      null, "Наименование организации или ИП"),
        
        INIT_TYPE          (BOOLEAN, new Virt ("DECODE(\"UUID_ORG\", NULL, 1, 0)"), "Тип инициатора (0 - организация, 1 - физическое лицо)"),
        INITIATOR          (STRING, new Virt ("DECODE(\"UUID_ORG\", NULL, \"ORG_LABEL\", \"IND_LABEL\")"), "Инициатор")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default: 
                    return true;
            }
        }
        
    }
    
    public VoteInitiator () {
        
        super ("tb_vote_initiators", "Инициаторы собрания собственников");
        
        cols (c.class);
        
        key ("uuid_protocol", c.UUID_PROTOCOL);
        key ("uuid_org", c.UUID_ORG);
        key ("uuid_ind", c.UUID_IND);
        
        trigger ("BEFORE INSERT",
                 "BEGIN "
                    + "IF :NEW.uuid_ind IS NOT NULL THEN "
                        + "SELECT "
                            + "pers.surname, "
                            + "pers.firstname, "
                            + "pers.patronymic, "
                            + "pers.is_female, "
                            + "pers.code_vc_nsi_95, "
                            + "pers.series, "
                            + "pers.number_, "
                            + "pers.issuedate, "
                            + "pers.issuer "
                        + "INTO "
                            + ":NEW.ind_surname, "
                            + ":NEW.ind_firstname, "
                            + ":NEW.ind_patronymic, "
                            + ":NEW.ind_isfemale, "
                            + ":NEW.ind_code_vc_nsi_95, "
                            + ":NEW.ind_series, "
                            + ":NEW.ind_number, "
                            + ":NEW.ind_issue_date, "
                            + ":NEW.ind_issuer "
                        + "FROM "
                            + "tb_prop_docs prop INNER JOIN vc_persons pers ON prop.uuid_person_owner = pers.uuid "
                        + "WHERE :NEW.uuid_ind = prop.uuid; "
                    + "ELSE "
                        + "SELECT "
                            + "org.ogrn, "
                            + "org.inn, "
                            + "org.kpp, "
                            + "org.okopf, "
                            + "org.label "
                        + "INTO "
                            + ":NEW.org_ogrn, "
                            + ":NEW.org_inn, "
                            + ":NEW.org_kpp, "
                            + ":NEW.org_okopf, "
                            + ":NEW.org_label "
                        + "FROM "
                            + "vc_orgs org WHERE :NEW.uuid_org = org.uuid; "
                    + "END IF; "
                + "END;"
                );
    }
    
}
