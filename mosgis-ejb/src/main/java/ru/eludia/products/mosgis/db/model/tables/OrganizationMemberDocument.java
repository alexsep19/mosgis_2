package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationParticipant;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.tables.OrganizationMemberDocumentLog;

public class OrganizationMemberDocument extends EnTable {

    public enum c implements EnColEnum {

        UUID_ORG                  (VocOrganization.class, "Товарищество, кооператив"),
        UUID_ORG_MEMBER           (VocOrganization.class, "Член товарищества, кооператива - организация"),
        UUID_PERSON_MEMBER        (VocPerson.class,       "Член товарищества, кооператива - физлицо"),
        UUID_ORG_AUTHOR           (VocOrganization.class, "Поставщик даных"),

        PARTICIPANT               (VocOrganizationParticipant.class
                , VocOrganizationParticipant.i.NOT_PARTICIPANT
                , "Участие в совете правления, ревизионной комиссии"
        ),
        DT_FROM_PARTICIPANT       (DATE, null, "Период избрания с"),
        DT_TO_PARTICIPANT         (DATE, null, "Период избрания по"),

        IS_CHAIRMAN               (BOOLEAN, null, "Избран председателем правления: 1 - Да, 0 - Нет"),
        DT_FROM_CHAIRMAN          (DATE, null, "Период правления с"),
        DT_TO_CHAIRMAN            (DATE, null, "Период правления по"),

        PHONE                     (STRING, null, "Номер телефона"),
        FAX                       (STRING, null, "Факс"),
        MAIL                      (STRING, null, "Адрес электронной почты"),

        DT_FROM                   (DATE, null, "Дата принятия в члены товарищества, кооператива"),
        DT_TO                     (DATE, null, "Дата исключения из членов товарищества, кооператива"),

        ENTRANCE_FEE              (NUMERIC, 12, 2, null, "Размер вступительных взносов"),
        CONTRIBUTION_SHARE        (NUMERIC, 12, 2, null, "Размер паевых взносов"),

        ID_LOG                    (OrganizationMemberDocumentLog.class, "Последнее событие редактирования")
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
                    return false;
                default:
                    return true;
            }
        }
    }

    public OrganizationMemberDocument () {

        super ("tb_org_member_docs", "Документы о членах товарищества, кооператива");

        cols   (c.class);

        key    ("uuid_org", c.UUID_ORG);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + "IF :NEW.is_deleted = 0 THEN "
                + " FOR i IN ("
                    + "SELECT "
                    + " root.dt_from "
                    + " , root.dt_to "
                    + " , vc_orgs.label    AS org_label "
                    + " , vc_persons.label AS person_label "
                    + " , org_parent.label AS parent_org_label "
                    + "FROM "
                    + " tb_org_member_docs root "
                    + " LEFT JOIN vc_orgs ON vc_orgs.uuid = root.uuid_org_member "
                    + " LEFT JOIN vc_persons ON vc_persons.uuid = root.uuid_person_member "
                    + " LEFT JOIN vc_orgs org_parent ON org_parent.uuid = root.uuid_org "
                    + "WHERE root.is_deleted = 0 "
                    + " AND (root.uuid_org_member = :NEW.uuid_org_member OR root.uuid_person_member = :NEW.uuid_person_member) "
                    + " AND (root.dt_to IS NULL OR root.dt_to   >= :NEW.dt_from) "
                    + " AND (:NEW.dt_to IS NULL OR root.dt_from <= :NEW.dt_to ) "
                    + " AND root.uuid <> NVL(:NEW.uuid, '00') "
                + ") LOOP"
                + " raise_application_error (-20000, "
                    + "NVL(i.org_label, i.person_label) || ' уже зарегистрирован в ' || i.parent_org_label || ' с ' "
                    + "|| TO_CHAR (i.dt_from, 'DD.MM.YYYY') "
                    + "|| CASE WHEN i.dt_to IS NULL THEN NULL ELSE ' по ' "
                    + "|| TO_CHAR (i.dt_to, 'DD.MM.YYYY') END "
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "

               + " IF :NEW.is_deleted = 0 THEN "
                + " IF :NEW.uuid_org_member IS NULL AND :NEW.uuid_person_member IS NULL THEN "
                    + "   raise_application_error (-20000, 'Укажите члена товарищества, кооператива - юрлицо или физлицо. Операция отменена.'); "
                    + " END IF; "

                    + " IF :NEW.participant = " + VocOrganizationParticipant.i.NOT_PARTICIPANT.getId() + " THEN "
                    + "   :NEW.dt_from_participant := NULL; "
                    + "   :NEW.dt_to_participant   := NULL; "
                    + " END IF; "

                    + " IF :NEW.dt_from_participant < :NEW.dt_from THEN "
                    + "   raise_application_error (-20000, 'Дата избрания в состав ревизионной комиссии не должна быть раньше даты принятия в члены товарищества, кооператива. Операция отменена.'); "
                    + " END IF; "

                    + " IF :NEW.dt_to_participant > :NEW.dt_to THEN "
                    + "   raise_application_error (-20000, 'Дата окончания срока избрания в состав ревизионной комиссии не должна быть позже даты выхода из членов товарищества, кооператива. Операция отменена.'); "
                    + " END IF; "

                    + " IF :NEW.is_chairman = 1 AND :NEW.dt_from_chairman IS NULL THEN "
                    + "   raise_application_error (-20000, 'Укажите период правления.'); "
                    + " END IF; "

                    + " IF :NEW.dt_from IS NULL "
                    + " AND :NEW.participant <> " + VocOrganizationParticipant.i.IN_COMMISSION_ALIEN.getId() + " THEN "
                    + "       raise_application_error (-20000, 'Укажите дату принятия в члены товарищества, кооператива.'); "
                    + " END IF; "
                + " END IF; "
        + "END;");
    }
}