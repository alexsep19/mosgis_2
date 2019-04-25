package ru.eludia.products.mosgis.jms.xl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFileLog;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlOrgPackItem;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisOrgCommonClient;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlOrgPackCheckQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class SliceOrgPacksMDB extends UUIDMDB<InXlFile> {
    
    @EJB
    public UUIDPublisher uuidPublisher;
    
    @EJB
    protected WsGisOrgCommonClient wsGisOrgClient;

    @Resource (mappedName = "mosgis.inXlOrgPackPollQueue")
    Queue inXlOrgPackPollQueue;            

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        MosGisModel m = (MosGisModel) db.getModel ();
        
        List<Map<String, Object>> list = db.getList (m
            .select  (InXlOrgPackItem.class
                , EnTable.c.UUID.lc ()
                , InXlOrgPackItem.c.OGRN.lc ()
                , InXlOrgPackItem.c.KPP.lc ()
            )
            .where   (InXlOrgPackItem.c.UUID_XL, uuid)
            .where   (InXlOrgPackItem.c.ERR.lc () + " IS NULL")
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
            
            final ExportOrgRegistryRequest rq = new ExportOrgRegistryRequest ();
            
            for (Map<String, Object> i: list) {

                final ExportOrgRegistryRequest.SearchCriteria sc = new ExportOrgRegistryRequest.SearchCriteria ();
                                
                String ogrn = DB.to.String (i.remove ("ogrn"));

                if (ogrn.length () == 15) {
                    sc.setOGRNIP (ogrn);
                }
                else {
                    sc.setOGRN (ogrn);
                    String kpp = DB.to.String (i.remove ("kpp"));
                    if (DB.ok (kpp)) {
                        
                        int len = kpp.length ();
                                
                        if (len == 9) {
                            sc.setKPP (kpp);
                        }
                        else {
                            StringBuilder sb = new StringBuilder ();
                            for (int j = 0; j < 9 - len; j ++) sb.append ('0');
                            sb.append (kpp);
                            sc.setKPP (sb.toString ());
                        }
                        
                    }
                        
                }

                rq.getSearchCriteria ().add (sc);                
                
                i.put (InXlOrgPackItem.c.UUID_PACK.lc (), uuidPack);
                
            }                

            try {

                db.update (OutSoap.class, DB.HASH (
                    "uuid",     uuidPack,
                    "uuid_ack", wsGisOrgClient.exportOrgRegistry (rq, UUID.fromString (uuidPack)).getMessageGUID ()
                ));

                db.update (InXlFileLog.class, DB.HASH (
                    "uuid",          uuidPack,
                    "uuid_out_soap", uuidPack
                ));

                uuidPublisher.publish (inXlOrgPackPollQueue, uuidPack);

            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, "Cannot import org", ex);
            }

            db.update (InXlOrgPackItem.class, list);

        }

    }
    
}