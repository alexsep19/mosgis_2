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
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocPublicPropertyContractFileType;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class PublicPropertyContractLogTest extends BaseTest {
    
    private PublicPropertyContract table;
    private PublicPropertyContractLog logTable;
    private PublicPropertyContractFile fileTable;
    private PublicPropertyContractFileLog fileLogTable;
    
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
            PublicPropertyContract.c.PAYMENT, "1.23",
            PublicPropertyContract.c.ID_LOG, null
        );
        
        jc       = JAXBContext.newInstance (ImportPublicPropertyContractRequest.class);
        schema   = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        table    = (PublicPropertyContract) model.get (PublicPropertyContract.class);
        logTable = (PublicPropertyContractLog) model.get (PublicPropertyContractLog.class);
        fileTable = (PublicPropertyContractFile) model.get (PublicPropertyContractFile.class);
        fileLogTable = (PublicPropertyContractFileLog) model.get (PublicPropertyContractFileLog.class);
        
        
    }

    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            

            db.d0 (new QP ("UPDATE tb_pp_ctr_files SET id_log = NULL WHERE uuid_ctr = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr_files__log WHERE uuid_object IN (SELECT uuid FROM tb_pp_ctr_files WHERE uuid_ctr = '00000000000000000000000000000000')"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr_files WHERE uuid_ctr = '00000000000000000000000000000000'"));

            db.d0 (new QP ("UPDATE tb_pp_ctr SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr__log WHERE uuid_object = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr WHERE uuid = '00000000000000000000000000000000'"));

        }

    }

//    @Test (expected = Test.None.class)
    public void testOrg () throws SQLException {        
        checkSample (table.new Sampler (commonPart, HASH (PublicPropertyContract.c.UUID_ORG_CUSTOMER, getOrgUuid ())).nextHASH ());
    }
    
    @Test (expected = Test.None.class)
    public void testPerson () throws SQLException {        
        checkSample (table.new Sampler (commonPart, HASH (PublicPropertyContract.c.UUID_PERSON_CUSTOMER, getPersonUuid ())).nextHASH ());
    }
    
    private void checkSample (Map<String, Object> sample) throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            db.insert (table, sample);
            String idLog = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);
            db.update (table, HASH (
                EnTable.c.UUID, uuid,
                PublicPropertyContract.c.ID_LOG, idLog
            ));
            
            db.insert (fileTable, HASH (
               EnTable.c.IS_DELETED, 0,
               AttachTable.c.ID_STATUS, 1,
               AttachTable.c.LABEL, "1.doc",
               AttachTable.c.LEN, 1,
               AttachTable.c.MIME, "application/octet-stream",
               PublicPropertyContractFile.c.UUID_CTR, uuid,
               PublicPropertyContractFile.c.ID_TYPE, VocPublicPropertyContractFileType.i.CONTRACT.getId ()
            ));            
            
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));
            PublicPropertyContractLog.addFilesForExport (db, r);
            check (r);
            
        }
        
    }

    private void check (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (PublicPropertyContractLog.toImportPublicPropertyContractRequest (r));
    }
        
}
