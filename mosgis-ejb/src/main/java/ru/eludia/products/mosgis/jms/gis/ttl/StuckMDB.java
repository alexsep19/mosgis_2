package ru.eludia.products.mosgis.jms.gis.ttl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

public abstract class StuckMDB<T extends Table> extends UUIDMDB<T> {

    Map<String, Object> getValues (DB db, UUID uuid, Map<String, Object> r) throws SQLException {            
        
        final Map<String, Object> values = HASH (
            "uuid", uuid,
            "id_ctr_status", VocGisStatus.i.FAILED_STATE.getId ()
        );
        
        UUID uuidOutSoap = (UUID) r.get ("uuid_out_soap");
        
        if (uuidOutSoap == null) {
            values.put ("uuid_out_soap", OutSoap.addExpired (db));
        }
        else {
            OutSoap.expire (db, uuidOutSoap);
        }
        
        return values;
        
    }
    
}
