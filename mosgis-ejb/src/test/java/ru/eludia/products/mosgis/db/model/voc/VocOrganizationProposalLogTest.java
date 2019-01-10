package ru.eludia.products.mosgis.db.model.voc;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal.c;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.organizations_registry.ImportSubsidiaryRequest;

public class VocOrganizationProposalLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");
    
    private VocOrganizationProposal table;
    private VocOrganizationProposalLog logTable;
    
    private Map<String, Object> commonPart;        

    public VocOrganizationProposalLogTest () throws Exception {
        
        super ();        
        
        jc            = JAXBContext.newInstance (ImportSubsidiaryRequest.class);
        schema        = AbstactServiceAsync.loadSchema ("organizations-registry/hcs-organizations-registry-types.xsd");
        
        table         = (VocOrganizationProposal)    model.get (VocOrganizationProposal.class);
        logTable      = (VocOrganizationProposalLog) model.get (VocOrganizationProposalLog.class);
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
            c.ID_TYPE, VocOrganizationTypes.i.SUBSIDIARY.getId (),
            c.ID_ORG_PR_STATUS, 10,
            c.ID_ORG_PR_STATUS_GIS, 10,            
            c.PARENT, getSomeUuid (VocOrganization.class),
            c.REGISTRATIONCOUNTRY, null,
            c.UUID_ORG, null,
            c.FIASHOUSEGUID, null,
            c.ID_LOG, null
        );        
        
    }

    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            

            db.d0 (new QP ("UPDATE vc_org_proposals SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM vc_org_proposals__log WHERE uuid_object = '00000000000000000000000000000000'"));

            db.d0 (new QP ("DELETE FROM vc_org_proposals WHERE uuid = '00000000000000000000000000000000'"));

        }

    }    

    @Test
    public void testInsert () throws SQLException {
                
        final Table.Sampler sampler = table.new Sampler (commonPart);
        
        try (DB db = model.getDb ()) {

            for (int i = 0; i < sampler.getCount (); i ++) {

                Map<String, Object> sample = sampler.nextHASH ();

                dump (sample);
                
                db.upsert (VocOrganizationProposal.class, sample);
                
                String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);

                db.update (table, HASH (
                    EnTable.c.UUID, uuid,
                    VocOrganizationProposal.c.ID_LOG, id
                ));
                
                Map<String, Object> r = db.getMap (logTable.getForExport (id));

                dump (r);

                validate (VocOrganizationProposalLog.toImportSubsidiaryRequest (r));
                
            }        

        }        

    }
    
}