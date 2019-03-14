package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposalLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal.c;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisOrgClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.organizations_registry.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportVocOrganizationProposalsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportVocOrganizationProposalMDB  extends GisPollMDB {

    @EJB
    WsGisOrgClient wsGisOrgClient;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
                
            .toOne (VocOrganizationProposalLog.class,     "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganizationProposal.class,        "AS r", "uuid").on ()
            .toOne (VocOrganization.class, "AS o"
                , VocOrganization.c.ORGPPAGUID.lc ()
            ).on ("r.uuid_org_owner=o.uuid")
                
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("o.orgppaguid");
        
        VocOrganizationProposal.Action action = VocOrganizationProposal.Action.forLogAction (VocAction.i.forName (r.get ("log.action").toString ()));
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);            
            
            final List<CommonResultType> commonResult = state.getImportResult ();                        
            
            if (commonResult == null || commonResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            final CommonResultType first = commonResult.get (0);
                                                
            for (CommonResultType.Error err: first.getError ()) throw new GisPollException (err);

            final Map<String, Object> h = statusHash (action.getOkStatus ());
            
            UUID orgRootEntityGuid = UUID.fromString (first.getGUID ());
            
            Map<String, Object> org = db.getMap (VocOrganizationProposal.class, r.get ("r.uuid"));
            
            org.remove ("uuid");
            org.remove ("id_log");
            
            org.put (VocOrganization.c.ORGROOTENTITYGUID.lc (), orgRootEntityGuid);
            
            db.insert (VocOrganization.class, org);
            
            h.put (VocOrganizationProposal.c.UUID_ORG.lc (), orgRootEntityGuid);

            update (db, uuid, r, h);

            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            update (db, uuid, r, statusHash (action.getFailStatus ()));            
            ex.register (db, uuid, r);
        }
        
    }

    private static Map<String, Object> statusHash (VocGisStatus.i status) {
        
        final byte id = status.getId ();
        
        return HASH (c.ID_ORG_PR_STATUS,     id,
            c.ID_ORG_PR_STATUS, id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {

logger.info ("h=" + h);

        h.put ("uuid", r.get ("r.uuid"));
        db.update (VocOrganizationProposal.class, h);

        h.put ("uuid", uuid);
        db.update (VocOrganizationProposalLog.class, h);

    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;

        try {
            rp = wsGisOrgClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }

        checkIfResponseReady (rp);

        return rp;

    }

}
