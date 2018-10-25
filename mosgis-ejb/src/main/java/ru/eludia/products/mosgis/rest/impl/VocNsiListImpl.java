package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.model.abs.Roster;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.rest.api.VocNsiListLocal;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.nsi.NsiMultipleRefTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiNsiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiOkeiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiStringField;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocRdCol;
import static ru.eludia.products.mosgis.db.model.voc.VocRdColType.i.REF;
import ru.eludia.products.mosgis.db.model.voc.VocRdList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.NsiLocal;

@Stateless
public class VocNsiListImpl implements VocNsiListLocal {
    
    @EJB
    NsiLocal nsi;

    private static final Logger logger = Logger.getLogger (VocNsiListImpl.class.getName ());
        
    @Override
    public JsonObject select (JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,

                ModelHolder.getModel ()
                    .select (VocRdList.class,
                        "modelid AS id", 
                        "name AS text", 
                        "parent")
                    .orderBy ("name"),

                ModelHolder.getModel ()
                    .select (VocRdCol.class,
                        "link_dictionary AS id", 
                        "name AS text")
                    .where ("property_value_type", REF.getId ())
                    .orderBy ("name"),

                ModelHolder.getModel ()
                    .select (VocNsiListGroup.class, "*")
                    .orderBy ("label"),

                ModelHolder.getModel ()
                    .select (VocNsiList.class,
                        "registrynumber AS id",
                        "name AS text",
                        "listgroup")
                    .orderBy ("name")

            );
            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    

    @Override
    public JsonObject getItem (String id) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            int registryNumber = Integer.parseInt (id);

            final JsonObject item = db.getJsonObject (
                ModelHolder.getModel ().get (VocNsiList.class, registryNumber, "*")
                .toMaybeOne (OutSoap.class, "id_status", "ts_rp", "err_code", "err_text").on ()
            );

            jb.add ("item", item);

            if (item.getInt ("out_soap.id_status", 0) < 3 || item.getString ("cols", "").isEmpty ()) return jb.build ();

            NsiTable table = getNsiTable (registryNumber, db);

            NsiOkeiRefField okeiField = table.getOkeiField ();

            if (okeiField != null) {

                String name = ModelHolder.getModel ().getName (VocOkei.class);

                QP qp = new QP ("SELECT code AS \"id\", national AS \"label\" FROM ");
                qp.append (name);
                qp.append (" WHERE code IN(SELECT DISTINCT ");
                qp.append (okeiField.getfName ());
                qp.append (" FROM ");
                qp.append (table.getName ());
                qp.append (')');

                jb.add (name, db.getJsonArray (qp));

            }

            for (NsiField f: table.getNsiFields ().values ()) {

                if (!(f instanceof NsiNsiRefField)) continue;

                NsiNsiRefField ref = (NsiNsiRefField) f;

                NsiTable t;

                try {
                    t = getNsiTable (ref.getRegistryNumber (), db);
                }
                catch (Exception ex) {
                    continue;
                }

                NsiStringField labelField = t.getLabelField ();

                if (labelField == null) continue;

                final String name = t.getName ();

                QP qp = new QP ("SELECT guid \"id\",");
                qp.append (labelField.getfName ());
                qp.append (" \"label\" FROM ");
                qp.append (name);
                qp.append (" WHERE guid IN(SELECT DISTINCT ");

                if (ref.isMultiple ()) {
                    qp.append ("guid_to FROM ");
                    qp.append (table.getName ());
                    qp.append ('_');
                    qp.append (ref.getName ());
                }
                else {
                    qp.append (ref.getfName ());
                    qp.append (" FROM ");
                    qp.append (table.getName ());
                }

                qp.append (')');

                try {
                    jb.add (name, db.getJsonArray (qp));
                }
                catch (Exception ex) {
                    continue;
                }

            }

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    private NsiTable getNsiTable (int registryNumber, final DB db) throws SQLException {

        try {
            return NsiTable.getNsiTable (registryNumber);
        }
        catch (ClassCastException ex) { // VocNsiPassportFieldsSrcTable
            return new NsiTable (db, registryNumber);
        }

    }

    private static final String [] absurd = {};

    @Override
    public JsonObject getLines (String id, JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            int registryNumber = Integer.parseInt (id);

            final JsonObject item = db.getJsonObject (
                ModelHolder.getModel ().get (VocNsiList.class, registryNumber, "*")
                .toMaybeOne (OutSoap.class, "id_status").on ()
            );

            if (item.getInt ("out_soap.id_status", 0) < 3 || item.getString ("cols", "").isEmpty ()) return jb.build ();

            NsiTable table = getNsiTable (registryNumber, db);

            db.adjustTable (table);

            final Roster<NsiField> nsiFields = table.getNsiFields ();

            List <String> cols = new ArrayList<> ();

            cols.add ("guid AS id");
            cols.add ("code");
            for (NsiField i: nsiFields.values ()) if (!i.isMultiple ()) cols.add (i.getCols ().get (0).getName ());

            Select select = ModelHolder.getModel ()
                .select (table, cols.toArray (absurd))
                .where   ("isactual", 1)
                .orderBy ("code")
                .limit (p.getInt ("offset"), p.getInt ("limit")
            );

            List<Map<String, Object>> lines = new ArrayList <> ();
            Map<String, Map<String, Object>> idx = new HashMap <> ();

            db.forEach (select, rs -> {
                Map<String, Object> r = db.HASH (rs);
                idx.put (r.get ("id").toString (), r);
                lines.add (r);        
            });        

            for (NsiMultipleRefTable refTable: table.getMultipleRefTables ()) {

                final String fName = refTable.getTargetField ().getfName ().toLowerCase ();

                db.adjustTable (refTable);

                String [] lastId = new String [] {null};
                JsonArrayBuilder [] jab = new JsonArrayBuilder [] {null};

                db.forEach (ModelHolder.getModel ()

                    .select  (refTable, "guid_from", "guid_to")
                    .where   ("guid_from IN", idx.keySet ().toArray ())
                    .orderBy ("guid_from")
                    .orderBy ("ord")

                    , rs -> {

                        DB.ResultGet rg = new DB.ResultGet (rs);

                        String guid_from = rg.getUUIDString ("guid_from");

                        if (!guid_from.equals (lastId [0])) {
                            if (jab [0] != null) idx.get (lastId [0]).put (fName, jab [0].build ());
                            jab [0] = Json.createArrayBuilder ();
                            lastId [0] = guid_from;
                        }

                        jab [0].add (rg.getUUIDString ("guid_to"));

                    }

                );

                if (jab [0] != null) idx.get (lastId [0]).put (fName, jab [0].build ());

            };

            jb.add ("records", DB.to.JsonArrayBuilder (lines));

            jb.add ("cnt", db.getCnt (select));

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    @Override
    public JsonObject doImport (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        nsi.importNsiItems (Integer.valueOf (id));

        return jb.build ();

    }

    @Override
    public JsonObject getVocs (JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        JsonArray ids = p.getJsonObject ("data").getJsonArray ("ids");
        int len = ids.size ();               
        Select [] sels = new Select [len];

        try (DB db = ModelHolder.getModel ().getDb ()) {
            for (int i = 0; i < len; i ++) sels [i] = getNsiTable (ids.getInt (i), db).getVocSelect ();
            db.addJsonArrays (jb, sels);
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

}