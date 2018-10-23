package ru.eludia.products.mosgis.jms.gis.poll;

import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ws.rs.core.Response;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseMgmtContractFilesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportMgmtContractFileMDB extends UUIDMDB<ContractFile> {

    @EJB
    protected RestGisFilesClient restGisFilesClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "attachmentguid")
            .toOne (Contract.class, "AS ctr").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")
        ;
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {       
        
        Response rp = restGisFilesClient.get ((UUID) r.get ("org.orgppaguid"), RestGisFilesClient.Context.HOMEMANAGEMENT, (UUID) r.get ("attachmentguid"));

logger.info ("rp=" + rp);

        db.update (ContractFile.class, HASH (
            "uuid", uuid,
            "mime", rp.getHeaderString ("Content-Type"),
            "len",  rp.getHeaderString ("Content-Length")
        ));

    }
    
}