package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Comparator;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.abs.NamedObject;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.TablesLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)

public class TablesImpl implements TablesLocal {
    
    final Comparator<NamedObject> byName = (o1, o2) -> {
        return o1.getName ().compareTo (o2.getName ());
    };

    @Override
    public JsonObject select (JsonObject p, User user) {
                
        JsonArrayBuilder ab = Json.createArrayBuilder ();
        
        ModelHolder.getModel ().getTables ().stream ().sorted (byName).forEach ( (t) -> {
            
            ab.add (Json.createObjectBuilder ()
                .add ("recid", t.getName ())
                .add ("label", t.getRemark ())
                .build ()
            );
            
        });
        
        return Json.createObjectBuilder ()
            .add ("tables", ab.build ())
        .build ();
        
    }

    @Override
    public JsonObject getItem (String id, User user) {
        
        final MosGisModel m = ModelHolder.getModel ();

        Table t = m.get (id);
        
        JsonObject item = Json.createObjectBuilder ()
            .add ("recid", t.getName ())
            .add ("label", t.getRemark ())
        .build ();

        JsonArrayBuilder cb = Json.createArrayBuilder ();
        JsonArrayBuilder rb = Json.createArrayBuilder ();

        t.getColumns ().values ().stream ().sorted (byName).forEach ((col) -> {

            JsonObjectBuilder job = Json.createObjectBuilder ()
                .add ("recid", col.getName ())
                .add ("label", col.getRemark ());
            
            if (col instanceof Ref) {
                Ref ref = (Ref) col;
                job.add ("ref", ref.getTargetTable ().getName ());
                rb.add (job.build ());
            }
            else {
                PhysicalCol phy = col.toPhysical ();
                job.add ("type", phy.getType ().getName ());
                job.add ("len", phy.getLength ());
                job.add ("prc", phy.getPrecision ());
                job.add ("nil", phy.isNullable ());
                job.add ("def", phy.getDef ());
                cb.add (job.build ());
            }
            
        });

        JsonArrayBuilder brb = Json.createArrayBuilder ();

        m.getTables ().stream ().sorted (byName).forEach ((tb) -> {
            
            tb.getColumns ().values ().stream ().sorted (byName).forEach ((col) -> {
                
                if (!(col instanceof Ref)) return;
                
                Ref ref = (Ref) col;
                
                String tn = ref.getTargetTable ().getName ();
                String cn = ref.getTargetCol ().getName ();
                
                brb.add (Json.createObjectBuilder ()
                    .add ("recid", tn + '.' + cn)
                    .add ("tn", tn)
                    .add ("cn", tn)
                    .add ("tl", ref.getTargetTable ().getRemark ())
                    .add ("cl", ref.getTargetCol ().getRemark ())
                );
                
            });            
            
        });

        return Json.createObjectBuilder ()
            .add ("item", item)
            .add ("cols", cb.build ())
            .add ("refs", rb.build ())
            .add ("back", brb.build ())
        .build ();

    }

}