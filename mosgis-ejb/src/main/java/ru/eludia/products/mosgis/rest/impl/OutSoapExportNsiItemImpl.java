package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.rest.api.OutSoapExportNsiItemLocal;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Stateless
public class OutSoapExportNsiItemImpl implements OutSoapExportNsiItemLocal {

    private static final Logger logger = Logger.getLogger (OutSoapExportNsiItemImpl.class.getName ());

    public final String order (JsonObject p, String def) {
        
        JsonArray sort = p.getJsonArray ("sort");
        
        if (sort == null) return def;
        
        if (sort.isEmpty ()) return def;
        
        final StringBuilder sb = new StringBuilder ();
        
        sort.stream ().map (v -> ((JsonObject) v)).forEach (o -> {
            
            if (sb.length () > 0) sb.append (',');
            
            sb.append (o.getString ("field"));
            
            if ("desc".equals (o.getString ("direction"))) sb.append (" DESC");
            
        });
        
        return sb.toString ();
        
    }    

    @Override
    public JsonObject getStats (JsonObject p) {
        
        JsonObject data = p.getJsonObject ("data");

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        if (data.get ("month") == null || data.getJsonArray ("month").isEmpty ()) return jb.build ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            final String prefix = "" + data.getInt ("year") + '-';

            Table outSoap = ModelHolder.getModel ().get (OutSoap.class);
            PhysicalCol ym = outSoap.getColumn ("ym").toPhysical ();

            QP qpi = new QP ("SELECT uuid, is_failed, TRUNC(ts) dt FROM ");

            qpi.append (outSoap.getName ());
            qpi.append (" WHERE svc = 'NsiServiceAsync' AND op LIKE 'exportNsi%Item' AND ym IN(");
            data.getJsonArray ("month").forEach (i -> {
                qpi.add ("?,", prefix + ((JsonString) i).getString (), ym);
            });
            qpi.setLastChar (')');

            if ("1".equals (data.getString ("is_failed", ""))) qpi.append (" AND is_failed = 1");

            QP qpo = new QP ("WITH soap AS (");
            qpo.add (qpi);
            qpo.append (") SELECT dt \"id\", COUNT(*) \"cnt\", SUM(is_failed) \"cnt_failed\" FROM soap GROUP BY dt ORDER BY dt");

            jb.add ("records", db.getJsonArray (qpo));

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject getErrors (String dt, JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.addJsonArrayCnt (jb, 
                ModelHolder.getModel ().select (OutSoap.class, "uuid AS id", "uuid_ack", "ts_rp", "err_code", "err_text")
                .where ("is_failed", 1)
                .and ("op LIKE", "exportNsi%Item")
                .and ("ts BETWEEN", dt + " 00:00:00", dt + " 23:59:59")
                .orderBy (order (p, "ts_rp"))
            );

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject getRq (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            jb.add ("xml", db.getString (OutSoap.class, id, "rq"));

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject getRp (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            jb.add ("xml", db.getString (ModelHolder.getModel ()
                .select (OutSoap.class, "rp")
                .where  ("uuid_ack", id)
            ));

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

}