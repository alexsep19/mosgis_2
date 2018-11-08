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

public class Owner extends View {

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
        sb.append (getName (PropertyDocument.class));
        sb.append (" root ");
        
        sb.append (" INNER JOIN ");
        sb.append (getName (Premise.class));
        sb.append (" premise ON premise.id = root.");
        sb.append (PropertyDocument.c.UUID_PREMISE.lc ());

        sb.append (" LEFT JOIN ");
        sb.append (getName (VocOrganization.class));
        sb.append (" org ON org.uuid = root.");
        sb.append (PropertyDocument.c.UUID_ORG_OWNER.lc ());

        sb.append (" LEFT JOIN ");
        sb.append (getName (VocPerson.class));
        sb.append (" person ON person.uuid = root.");
        sb.append (PropertyDocument.c.UUID_PERSON_OWNER.lc ());
                
        return sb.toString ();
        
    }


    public enum c implements ColEnum {
        
        ID                        (EnTable.c.UUID),        
        IS_DELETED                (EnTable.c.IS_DELETED),

        ID_TYPE                   (PropertyDocument.c.ID_TYPE),
        PRC                       (PropertyDocument.c.PRC),
        NO                        (PropertyDocument.c.NO),
        ISSUER                    (PropertyDocument.c.ISSUER),
        DT                        (PropertyDocument.c.DT),
        DT_TO                     (PropertyDocument.c.DT_TO),
        UUID_PREMISE              (PropertyDocument.c.UUID_PREMISE),
        UUID_ORG                  (PropertyDocument.c.UUID_ORG),

        UUID_HOUSE                (Premise.c.UUID_HOUSE),
        LABEL                     (Premise.c.LABEL),
        TOTALAREA                 (Premise.c.TOTALAREA),

        OWNER_LABEL               (STRING, "ФИО/Наименование организации"),
        OWNER_LABEL_UC            (STRING, "ФИО/НАИМЕНОВАНИЕ ОРГАНИЗАЦИИ"),
        
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
        private c (PropertyDocument.c c) {this (c, "root");};
        private c (Premise.c c)          {this (c, "premise");};
        
        public String getSql () {
            
            if (sql != null) return sql;
            
            switch (this) {
                case OWNER_LABEL:    return "NVL(person.label, org.label) ";
                case OWNER_LABEL_UC: return "UPPER(" + OWNER_LABEL.getSql () + ") ";
                default: throw new IllegalStateException ("SQL expression not defined for " + this);
            }

        }

    }

    public Owner () {

        super ("tb_owners", "Собственники");
        
        cols   (c.class);
        pk     (c.ID);
        
    }
                
}