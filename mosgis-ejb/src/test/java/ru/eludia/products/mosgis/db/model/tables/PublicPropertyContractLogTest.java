package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class PublicPropertyContractLogTest extends BaseTest {
    
    private Table table;
    private PublicPropertyContractLog logTable;
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");
    private final Map<String, Object> commonPart;
    
    public PublicPropertyContractLogTest () throws Exception {
        
        super ();
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
            PublicPropertyContract.c.UUID_ORG, getOrgUuid (),
            PublicPropertyContract.c.FIASHOUSEGUID, "ef37b493-e94f-4f27-9e86-f4cd80f1057f",
            PublicPropertyContract.c.UUID_PERSON_CUSTOMER, null,
            PublicPropertyContract.c.UUID_ORG_CUSTOMER, null,
            PublicPropertyContract.c.ID_CTR_STATUS, 10,
            PublicPropertyContract.c.ID_CTR_STATUS_GIS, 10,
            PublicPropertyContract.c.ID_CTR_STATE_GIS, 10,
            PublicPropertyContract.c.ID_LOG, null
        );
        
        jc       = JAXBContext.newInstance (ImportPublicPropertyContractRequest.class);
        schema   = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        table    = model.get (PublicPropertyContract.class);
        logTable = (PublicPropertyContractLog) model.getLogTable (table);
        
    }
    
    @Before
    @After
    public void clean () throws SQLException {
        
        try (DB db = model.getDb ()) {            
            db.d0 (new QP ("UPDATE tb_pp_ctr SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr__log WHERE uuid_object = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr WHERE uuid = '00000000000000000000000000000000'"));
        }
        
    }
    
    @Test (expected = Test.None.class)
    public void test () throws SQLException {        
        checkSample (table.new Sampler (commonPart).nextHASH ());
    }
    
    protected String getOrgUuid () throws SQLException {
        try (DB db = model.getDb ()) {
            return db.getString (model.select (VocOrganization.class, "uuid").where ("is_deleted", 0));
        }
    }

    private void checkSample (Map<String, Object> sample) throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            db.insert (table, sample);
            String idLog = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);
            db.update (table, HASH (
                EnTable.c.UUID, uuid,
                PublicPropertyContract.c.ID_LOG, idLog
            ));
            
            Map<String, Object> map = db.getMap (logTable.get (idLog));
            
            dump (map);
            
        }
        
    }

    private void check (final Map<String, Object> r) throws IllegalStateException {
        dump (r);        
        validate (PublicPropertyContractLog.toImportPublicPropertyContractRequest (r));
    }
        
}
