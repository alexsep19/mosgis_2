package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.sql.build.SQLBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class OrganizationMember extends View {

    @Override
    public String getSQL () {

        StringBuilder sb = new StringBuilder ("SELECT ");

        for (c c: c.values ()) {
            sb.append (c.getSql ());
            sb.append (' ');
            sb.append (c.lc ());
            sb.append (',');
        }

        SQLBuilder.setLastChar (sb, ' ');

        sb.append (" FROM ");
        sb.append (getName (OrganizationMemberDocument.class));
        sb.append (" root ");

        sb.append(" LEFT JOIN ");
        sb.append(getName(VocOrganization.class));
        sb.append(" org_auth ON org_auth.uuid = root.");
        sb.append(OrganizationMemberDocument.c.UUID_ORG_AUTHOR.lc());

        sb.append (" LEFT JOIN ");
        sb.append (getName (VocOrganization.class));
        sb.append (" org ON org.uuid = root.");
        sb.append (OrganizationMemberDocument.c.UUID_ORG_MEMBER.lc ());

        sb.append (" LEFT JOIN ");
        sb.append (getName (VocPerson.class));
        sb.append (" person ON person.uuid = root.");
        sb.append (OrganizationMemberDocument.c.UUID_PERSON_MEMBER.lc ());

        return sb.toString ();

    }


    public enum c implements ColEnum {

        ID                        (EnTable.c.UUID),
        IS_DELETED                (EnTable.c.IS_DELETED),

        PARTICIPANT               (OrganizationMemberDocument.c.PARTICIPANT),
        DT_FROM_PARTICIPANT       (OrganizationMemberDocument.c.DT_FROM_PARTICIPANT),
        DT_TO_PARTICIPANT         (OrganizationMemberDocument.c.DT_TO_PARTICIPANT),

        DT_FROM                   (OrganizationMemberDocument.c.DT_FROM),
        DT_TO                     (OrganizationMemberDocument.c.DT_TO),

        UUID_ORG                  (OrganizationMemberDocument.c.UUID_ORG),

        ID_TYPE                   (STRING, "Тип лица: 0 - физлицо, 1 - юрлицо"),
        UUID_ORG_MEMBER           (OrganizationMemberDocument.c.UUID_ORG_MEMBER),
        UUID_PERSON_MEMBER        (OrganizationMemberDocument.c.UUID_PERSON_MEMBER),

        LABEL                     (STRING, "ФИО/Наименование члена товарищества, кооператива"),
        LABEL_UC                  (STRING, "ФИО/НАИМЕНОВАНИЕ ЧЛЕНА ТОВАРИЩЕСТВА, КООПЕРАТИВА"),

        AUTHOR_LABEL              (STRING, "Источник данных"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private String sql = null;

        private c (Type type, Object... p) {col = new Col (this, type, p);}

        private c (ColEnum c, String tableAlias) {
            this.col = c.getCol ().clone (lc ());
            sql = tableAlias + '.' + c.lc ();
        };

        private c (EnTable.c c)          {this (c, "root");};
        private c (OrganizationMemberDocument.c c) {this (c, "root");};

        public String getSql () {

            if (sql != null) return sql;

            switch (this) {
                case LABEL:    return "NVL(person.label, org.label) ";
                case AUTHOR_LABEL:   return "NVL(org_auth.label, 'Администратор')";
                case LABEL_UC: return "UPPER(" + LABEL.getSql () + ") ";
                case ID_TYPE: return "CASE WHEN uuid_org_member IS NOT NULL THEN 0 ELSE 1 END";
                default: throw new IllegalStateException ("SQL expression not defined for " + this);
            }

        }

    }

    public OrganizationMember () {

        super ("tb_org_members", "Члены товарищества, кооператива");

        cols   (c.class);
        pk     (c.ID);

    }

}