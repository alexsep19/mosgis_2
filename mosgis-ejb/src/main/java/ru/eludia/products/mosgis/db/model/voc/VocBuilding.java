package ru.eludia.products.mosgis.db.model.voc;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.tables.ActualCaChObject;
import ru.eludia.products.mosgis.db.model.tables.ActualSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;

public class VocBuilding extends Table {
    
    private static final Logger logger = Logger.getLogger (VocBuilding.class.getName ());
    
    private final String LABELSTR = "s_formalname || ' ' || s_shortname "
                         + "|| var_house || var_build || var_shortname";
    
    public VocBuilding () {
        
        super ("vc_buildings", "Здания, сооружения");
        
        pk    ("houseguid",    Type.UUID,                                         "Код здания в ФИАС");        
        
        col   ("housenum",     Type.STRING, 20, null,                             "Номер дома");
        col   ("buildnum",     Type.STRING, 10, null,                             "Номер корпуса");
        col   ("strucnum",     Type.STRING, 10, null,                             "Номер строения");
        fk    ("strstatus",    VocBuildingStructure.class,                        "Признак строения");

        fk    ("eststatus",    VocBuildingEstate.class,                           "Признак владения");

        col   ("postalcode",   Type.INTEGER, 6, null,                             "Почтовый индекс");
        col   ("okato",        Type.INTEGER, 11, null,                            "ОКАТО");
        col   ("oktmo",        Type.INTEGER, 11, null,                            "ОКТМО");
        col   ("cadnum",       Type.STRING, 100, null,                            "Кадастровый номер");
        
        col   ("livestatus",   Type.BOOLEAN, Bool.TRUE,                           "1 для актуальных записей, 0 для удалённых");

        fk    ("aoguid",       VocStreet.class,                                   "Улица (площадь и т. п.)");
        fk    ("uuid_in_fias", InFias.class,                                      "Последний пакет импорта");
        
        col   ("house_label",  Type.STRING,  new Virt ("DECODE(HOUSENUM,NULL,NULL,'д. '||HOUSENUM)"),  "д. ...");
        col   ("build_label",  Type.STRING,  new Virt ("DECODE(BUILDNUM,NULL,NULL,'корп. '||BUILDNUM)"),  "корп. ...");
        
        col   ("label",        Type.STRING,  null,                                "Адрес");
        col   ("label_uc",     Type.STRING,  new Virt ("UPPER(\"LABEL\")"),       "АДРЕС");
        
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