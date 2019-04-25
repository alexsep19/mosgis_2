package ru.eludia.products.mosgis.jms.xl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlOrgPackItem;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlOrgPackCheckQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class SliceOrgPacksMDB extends UUIDMDB<InXlFile> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        MosGisModel m = (MosGisModel) db.getModel ();
        
        List<Map<String, Object>> list = db.getList (m
            .select  (InXlOrgPackItem.class, "uuid")
            .where   (InXlOrgPackItem.c.UUID_XL, uuid)
            .where   (InXlOrgPackItem.c.UUID_PACK.lc () + " IS NULL")
            .orderBy (InXlOrgPackItem.c.ORD)
            .limit (0, 100)
        );
        
        if (list.isEmpty ()) {
            
            db.update (InXlFile.class, DB.HASH (
                EnTable.c.UUID, uuid,
                InXlFile.c.ID_STATUS, VocFileStatus.i.PROCESSED_OK
            ));
            
        }
        else {

            String uuidPack = m.createIdLog (db, getTable (), null, uuid, VocAction.i.APPROVE);

            list.forEach ((i) -> i.put (InXlOrgPackItem.c.UUID_PACK.lc (), uuidPack));

            db.update (InXlOrgPackItem.class, list);

        }

    }
    
}