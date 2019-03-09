package ru.eludia.products.mosgis.db.model.tables;

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
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ws.soap.impl.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class AgreementPaymentLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");

    private AgreementPayment table;
    private AgreementPaymentLog logTable;
    
    private final Map<String, Object> commonPart;
    
    public AgreementPaymentLogTest () throws Exception {
        
        super ();        
        
        jc       = JAXBContext.newInstance (ImportPublicPropertyContractRequest.class);
        schema   = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        
        table    = (AgreementPayment) model.get (AgreementPayment.class);
        logTable = (AgreementPaymentLog) model.get (AgreementPaymentLog.class);        
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
//            AgreementPayment.c.UUID_CTR, getSomeUuid (PublicPropertyContract.class),
            AgreementPayment.c.AGREEMENTPAYMENTVERSIONGUID, null,
            AgreementPayment.c.REASONOFANNULMENT, null,
            AgreementPayment.c.ID_LOG, null, 
            AgreementPayment.c.ID_AP_STATUS, 10, 
            AgreementPayment.c.ID_AP_STATUS_GIS, 10, 
            AgreementPayment.c.DEBT, "1.5",
            AgreementPayment.c.BILL, "2.3",
            AgreementPayment.c.PAID, "0.7"
        );
        
    }
    
    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            

            db.d0 (new QP ("UPDATE tb_pp_ctr_ap SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr_ap__log WHERE uuid_object = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_pp_ctr_ap WHERE uuid = '00000000000000000000000000000000'"));

        }

    }
    
    private String createData (final DB db, Map<String, Object> sample) throws SQLException {
        
        db.insert (table, sample);        
        
        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);        
        
        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            AgreementPayment.c.ID_LOG, id
        ));                
        
        return id;
        
    }        
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        Map<String, Object> sample = table.new Sampler (commonPart, rr).nextHASH ();
        sample.remove (AgreementPayment.c.IS_ANNULED.lc ());

        try (DB db = model.getDb ()) {            
            
            String uuidCtr = db.getString (model
                .select (PublicPropertyContract.class, "uuid")
                .where (PublicPropertyContract.c.CONTRACTVERSIONGUID.lc () + " IS NOT NULL")
                .where (EnTable.c.IS_DELETED, 0)
            );

            if (!DB.ok (uuidCtr)) throw new IllegalStateException ("PublicPropertyContract with non empty CONTRACTVERSIONGUID not found");
            sample.put (AgreementPayment.c.UUID_CTR.lc (), uuidCtr);
            
            String idLog = createData (db, sample);           
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));

            if (DB.ok (r.get (AgreementPayment.c.IS_ANNULED.lc ()))) {
                checkAnnul (r);
            }
            else {
                checkImport (r);
            }

        }
        
    }    
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (AgreementPaymentLog.toImportPublicPropertyContractRequest (r));
    }
    
    private void checkAnnul (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (AgreementPaymentLog.toImportPublicPropertyContractAnnulRequest (r));
    }

    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }

    @Test
    public void testUpdate () throws SQLException {

        checkSample (HASH (
            AgreementPayment.c.AGREEMENTPAYMENTVERSIONGUID, UUID.randomUUID ()
        ));

    }
    
    @Test
    public void testDelete () throws SQLException {

        checkSample (HASH (
            AgreementPayment.c.AGREEMENTPAYMENTVERSIONGUID, UUID.randomUUID (),
            AgreementPayment.c.REASONOFANNULMENT, "ÄNNUL"
        ));

    }
    
}