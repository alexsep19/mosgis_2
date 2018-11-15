package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class VoteInitiator extends EnTable {
    
    public enum c implements ColEnum {
        
        UUID_ORG      (VocOrganization.class,            "Организация"    ),
        UUID_IND      (PropertyDocument.class,           "Физическое лицо"),
        UUID_PROTOCOL (VotingProtocol.class,             "Протокол"),
        
        ID_LOG     (VoteInitiatorLog.class,     "Последнее событие редактирования"),
        IS_DELETED (BOOLEAN, Bool.FALSE,        "1, если запись удалена; иначе 0"),
        
        IND_SURNAME        (STRING, 256, null, "Фамилия"),
        IND_FIRSTNAME      (STRING, 256, null, "Имя"),
        IND_PATRONYMIC     (STRING, 256, null, "Отчество"),
        IND_ISFEMALE       (BOOLEAN,     null, "Пол (1 - женский, 0 - мужской)"),
        IND_CODE_VC_NSI_95 (STRING, 20,  null, "Код документа, ужостоверяющего личность (НСИ 95)"),
        IND_SERIES         (STRING, 45,  null, "Серия документа, удостоверяющего личность"),
        IND_NUMBER         (STRING, 45,  null, "Номер документа, удостоверяющего личность"),
        IND_ISSUE_DATE     (DATE,        null, "Дата выдачи документа, удостоверяющего личность"),
        IND_ISSUER         (STRING,      null, "Кем выдан документ, удостоверяющий личность"),
        
        INIT_TYPE          (BOOLEAN, new Virt ("DECODE(\"UUID_ORG\", NULL, 1)"), "Тип инициатора (0 - организация, 1 - физическое лицо)")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
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
                            + "pers.issue_date, "
                            + "pers.issuer "
                        + "INTO "
                            + ":NEW.ind_surname, "
                            + ":NEW.ind_firstname, "
                            + ":NEW.ind_patronymic, "
                            + ":NEW.ind_is_female, "
                            + ":NEW.ind_code_vc_nsi_95, "
                            + ":NEW.ind_series, "
                            + ":NEW.ind_number, "
                            + ":NEW.ind_issue_date, "
                            + ":NEW.ind_issuer "
                        + "FROM "
                            + "tb_prop_docs prop" + "INNER JOIN " + "vc_persons pers" + "ON " + "prop.uuid_person_owner = pers.uuid "
                        + "WHERE :NEW.uuid_ind = prop.uuid "
                    + "END IF; "
                + "END;"
                );
    }
    
}
