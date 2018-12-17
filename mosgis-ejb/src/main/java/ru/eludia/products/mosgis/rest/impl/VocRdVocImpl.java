package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.rest.api.VocRdVocLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.rd.RdTable;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocRdCol;
import static ru.eludia.products.mosgis.db.model.voc.VocRdCol.RD_NSI_REF_COL_NAME;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.RdMBean;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocRdVocImpl implements VocRdVocLocal {

    private static final Logger logger = Logger.getLogger (VocRdVocImpl.class.getName ());
    
    @EJB
    RdMBean rd;
        
    @Override
    public JsonObject getItem (String id) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            JsonObject item = db.getJsonObject (ModelHolder.getModel ()

                .select (VocRdCol.class,
                    "id",
                    "name AS label", 
                    RD_NSI_REF_COL_NAME)

                .where ("link_dictionary", id)

            );

            jb.add ("item", item);

            String code = item.getString (RD_NSI_REF_COL_NAME, "");

            if (!code.isEmpty ()) {

                NsiTable nsiFieldsTable = NsiTable.getPassportFieldsTable (db);        

                String refName = nsiFieldsTable.getColNameByRemarkPrefix ("Справочник");

                JsonObjectBuilder nsi = Json.createObjectBuilder ();

                String ref = db.getString (ModelHolder.getModel ()
                        .select (nsiFieldsTable, refName + " AS ref")
                        .where  ("code", code)
                );

                if (ref != null && !ref.isEmpty ()) {

                    nsi.add ("ref", ref);

                    NsiTable voc;

                    try {
                        voc = NsiTable.getNsiTable (Integer.valueOf (ref));
                    }
                    catch (Exception ex) {
                        voc = null;
                    }

                    if (voc != null) {

                        nsi.add ("label", voc.getRemark ());

                        String label = voc.getLabelField ().getfName ();

                        nsi.add ("items", db.getJsonArray (ModelHolder.getModel ()
                            .select (voc, "code AS id", label + " AS label")
                            .where ("isactual", 1)
                            .orderBy (label))
                        );

                    }
                    else {

                        String label = db.getString (ModelHolder.getModel ().get (VocNsiList.class, ref, "name"));

                        nsi.add ("label", label == null ? "НСИ " + ref: label);

                    }

                    jb.add ("nsi", nsi);

                }

            }        
            

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

            RdTable rdTable = new RdTable (db, Integer.valueOf (id));

            db.addJsonArrayCnt (jb, ModelHolder.getModel ()
                .select  (rdTable, "*")
                .orderBy ("name")
                .limit (p.getInt ("offset"), p.getInt ("limit"))
            );
            
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
            rd.importRdIdNames (Integer.valueOf (id));            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject doSetNsi (String id, JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            JsonObject data = p.getJsonObject ("data");

            String code = data.getString ("code");

            RdTable t = new RdTable (db, Integer.valueOf (id));

            db.begin ();

                db.d0 ("UPDATE " + t.getName () + " SET nsi_code=? WHERE nsi_code=?", null, code);

                db.upsert (t, data.getJsonArray ("ids").stream ().map (_id -> HASH (
                    "id",       _id.toString (),
                    "nsi_code", code
                )), null);

            db.commit ();

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

}