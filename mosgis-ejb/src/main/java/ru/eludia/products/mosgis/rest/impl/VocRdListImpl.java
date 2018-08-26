package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.rest.api.VocRdListLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocRdCol;
import static ru.eludia.products.mosgis.db.model.voc.VocRdCol.RD_NSI_REF_COL_NAME;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.eludia.products.mosgis.db.model.voc.VocRdList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.RdMBean;

@Stateless
public class VocRdListImpl implements VocRdListLocal {

    private static final Logger logger = Logger.getLogger (VocRdListImpl.class.getName ());
    
    @EJB
    RdMBean rd;
        
    @Override
    public JsonObject getItem (String id) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
                        
            jb.add ("item", db.getJsonObject (
                ModelHolder.getModel ().get (
                    VocRdList.class, id, "*")
                )
            );

            NsiTable nsiFieldsTable = NsiTable.getPassportFieldsTable (db);

            db.addJsonArrays (jb, ModelHolder.getModel ()
                .select (nsiFieldsTable
                    , "code AS id"
                    , nsiFieldsTable.getLabelField ().getfName ()            + " AS label"
                    , nsiFieldsTable.getColNameByRemarkPrefix ("Тип")        + " AS tp"
                    , nsiFieldsTable.getColNameByRemarkPrefix ("Сортировка") + " AS ord"
                )
                .where ("isactual", 1)
                .and ("code NOT IN", ModelHolder.getModel ()
                    .select (VocRdCol.class, RD_NSI_REF_COL_NAME)
                    .where ("object_model_id <>", id)
                    .where (RD_NSI_REF_COL_NAME + " IS NOT NULL")
                )
            );

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject getLines (String id, JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            final Select s = ModelHolder.getModel ()
                .select  (VocRdCol.class, "*")
                .toOne   (VocRdColType.class, "label", "gis_label").on ()
                .where   ("object_model_id", id)
                .orderBy ("vc_rd_cols.name")
                .limit (p.getInt ("offset"), p.getInt ("limit")
            );

            JsonArrayBuilder items = Json.createArrayBuilder ();

            db.forEach (s, rs -> {            

                final JsonObjectBuilder job = db.getJsonObjectBuilder (rs);

                int link_dictionary = rs.getInt ("link_dictionary");

                if (link_dictionary != 0) job.add ("cnt_nsi_codes", db.getInteger (new QP ("SELECT COUNT(*) FROM vc_rd_" + link_dictionary + " WHERE nsi_code IS NOT NULL")));

                items.add (job);

            });

            jb.add ("items", items);

            jb.add ("cnt", db.getCnt (s));
            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject doImport (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try {
            
            rd.importRdModel (Integer.valueOf (id));
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject select (JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
/* is it called at all?
        try (DB db = model.getDb ()) {

            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
*/        
        return jb.build ();

    }

    @Override
    public JsonObject doUpdate (JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.upsert (VocRdCol.class, 

                p.getJsonObject ("data").getJsonArray ("vc_rd_cols").stream ().map (i -> (JsonObject) i).map (i -> HASH (
                    "id",                i.getInt    ("id"),
                    RD_NSI_REF_COL_NAME, i.getString (RD_NSI_REF_COL_NAME)
                )).collect (Collectors.toList ())

            , null);
            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }

}