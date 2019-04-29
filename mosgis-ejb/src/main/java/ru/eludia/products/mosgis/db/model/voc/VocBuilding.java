package ru.eludia.products.mosgis.db.model.voc;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.tables.ActualCaChObject;
import ru.eludia.products.mosgis.db.model.tables.ActualSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;

public class VocBuilding extends Table {
    
    private static final Logger logger = Logger.getLogger (VocBuilding.class.getName ());
    
    private final String LABELSTR = "s_formalname || ' ' || s_shortname "
                         + "|| var_house || var_build || var_shortname";
    
    public static final String TABLE_NAME = "vc_buildings";
    
    public enum c implements EnColEnum {

        HOUSEGUID	(Type.UUID,                                         "Код здания в ФИАС"),        

        HOUSENUM	(Type.STRING, 20, null,                             "Номер дома"),
        BUILDNUM	(Type.STRING, 10, null,                             "Номер корпуса"),
        STRUCNUM	(Type.STRING, 10, null,                             "Номер строения"),
        STRSTATUS	(VocBuildingStructure.class,                        "Признак строения"),

        ESTSTATUS	(VocBuildingEstate.class,                           "Признак владения"),

        POSTALCODE	(Type.INTEGER, 6, null,                             "Почтовый индекс"),
        OKATO		(Type.INTEGER, 11, null,                            "ОКАТО"),
        OKTMO		(Type.INTEGER, 11, null,                            "ОКТМО"),
        CADNUM		(Type.STRING, 100, null,                            "Кадастровый номер"),

        LIVESTATUS	(Type.BOOLEAN, Bool.TRUE,                           "1 для актуальных записей, 0 для удалённых"),

        AOGUID		(VocStreet.class,                                   "Улица (площадь и т. п.)"),
        UUID_IN_FIAS	(InFias.class,                                      "Последний пакет импорта"),

        HOUSE_LABEL	(Type.STRING,  new Virt ("DECODE(HOUSENUM,NULL,NULL,'д. '||HOUSENUM)"),  "д. ..."),
        BUILD_LABEL	(Type.STRING,  new Virt ("DECODE(BUILDNUM,NULL,NULL,'корп. '||BUILDNUM)"),  "корп. ..."),

        LABEL		(Type.STRING,  null,                                "Адрес"),
        LABEL_UC	(Type.STRING,  new Virt ("UPPER(\"LABEL\")"),       "АДРЕС"),
        UUID    	(Type.UUID,    new Virt ("NVL(\"HOUSEGUID\",'00')"),           "uuid"),
        IS_DELETED    	(Type.NUMERIC, 1, 0, new Virt ("0+0"),           "всегда 0"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID:
                case IS_DELETED:
                case HOUSEGUID:
                    return false;
                default:
                    return true;
            }
        }        

    }
    

    public VocBuilding () {
        
        super (TABLE_NAME, "Здания, сооружения");
        
        cols (c.class);
        pk   (c.HOUSEGUID);        
        
        key   ("label_uc", "label_uc");
        
        trigger ("BEFORE INSERT OR UPDATE",
                 "DECLARE "
                    + "s_formalname VARCHAR2(4000 BYTE); "
                    + "s_shortname VARCHAR2(4000 BYTE); " 
                    + "ss_shortname VARCHAR2(10 BYTE); "
                         
                    + "var_house VARCHAR2(4000 BYTE); "
                    + "var_build VARCHAR2(4000 BYTE); "
                    + "var_shortname VARCHAR2(4000 BYTE); " +
                 "BEGIN "
                    + "SELECT formalname, shortname "
                    + "INTO s_formalname, s_shortname "
                    + "FROM vc_streets "
                    + "WHERE vc_streets.aoguid = :NEW.aoguid; "
                    
                    + "SELECT shortname "
                    + "INTO ss_shortname "
                    + "FROM vc_fias_strstat "
                    + "WHERE vc_fias_strstat.strstatid = :NEW.strstatus; "
                         
                    + "IF :NEW.house_label IS NULL THEN var_house := NULL; "
                    + "ELSE var_house := ', ' || :NEW.house_label; "
                    + "END IF; "
                    
                    + "IF :NEW.build_label IS NULL THEN var_build := NULL; "
                    + "ELSE var_build := ', ' || :NEW.build_label; "
                    + "END IF; "
                         
                    + "IF ss_shortname IS NULL THEN var_shortname := NULL; "
                    + "ELSE var_shortname := ', ' || ss_shortname || '. ' || :NEW.strucnum; "
                    + "END IF; "
                         
                    + ":NEW.label := " + LABELSTR + "; " +
                 "END;"
        );
        
    }

    public static void addCaCh (DB db, JsonObjectBuilder jb, Object fiashouseguid) throws SQLException {
        
        JsonObject caCh = db.getJsonObject (db.getModel ()
            .select (ActualCaChObject.class, "AS root", "uuid", "id_ctr_status_gis", "is_own")
            .toOne (VocOrganization.class, "AS org", "uuid", "label").on ()
            .toMaybeOne (Contract.class, "AS ctr", "uuid", "docnum", "signingdate").on ()
            .toMaybeOne (Charter.class, "AS ca", "uuid").on ()
            .where ("fiashouseguid", fiashouseguid)
            .orderBy ("root.is_own DESC")
            .orderBy ("root.id_ctr_status_gis")
        );

        if (caCh != null) jb.add ("cach", caCh);
        
    }
    
    public static void addSRCa (DB db, JsonObjectBuilder jb, Object fiashouseguid, Object userOrgUuid) throws SQLException {
        
        JsonObject srCa = db.getJsonObject (db.getModel ()
            .select     (ActualSupplyResourceContractObject.class, "AS root", "*")
            .toOne      (VocOrganization.class, "AS org", "uuid", "label").on ()
            .toMaybeOne (SupplyResourceContract.class, "AS ctr", "uuid", "contractnumber", "signingdate").on ()
            .where      ("fiashouseguid", fiashouseguid)
            .and        ("uuid_org", userOrgUuid)
            .orderBy    ("root.id_ctr_status")
        );
        
        if (srCa != null) jb.add ("srca", srCa);
        
    }

}