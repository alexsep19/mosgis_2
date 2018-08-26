package ru.eludia.products.mosgis.db.model.incoming.rec.statusViews;

import ru.eludia.products.mosgis.db.model.incoming.json.InImportHouses;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.incoming.rec.InUpsertHouse;

public class InHousePendingTransports extends View {

    public InHousePendingTransports () {
        
        super  ("in_tb_houses_pending",                        "Необработанные транспортные объекты на регистрацию/изменение МКД/ЖД");
        
        pk     ("uuid",           Type.UUID,                   "Ключ (TransportGUID)");
        
    }

    private void append (StringBuilder sb, Class table) {        

        sb.append ("SELECT ");
        sb.append ( "js.uuid ");      
        sb.append ("FROM ");
        
        sb.append ( getName (InImportHouses.class));
        sb.append ( " js ");
        
        sb.append ( "INNER JOIN ");
        sb.append ( getName (table));
        sb.append ( " tb");
        sb.append ( " ON tb.uuid_in_import = js.uuid ");
       
        sb.append ( "WHERE tb.ts_done IS NULL");
        
    }

    @Override
    public final String getSQL () {
        
        StringBuilder sb = new StringBuilder ();
        
        append (sb, InUpsertHouse.class);
                
        return sb.toString ();
    
    }

}