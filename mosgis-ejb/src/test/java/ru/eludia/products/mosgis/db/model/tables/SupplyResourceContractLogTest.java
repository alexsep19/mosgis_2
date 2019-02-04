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
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest;

public class SupplyResourceContractLogTest extends BaseTest {

    private static final UUID uuid = UUID.fromString ("810c9deb-1b90-715c-e053-0100007fb180");

    private SupplyResourceContract table;
    private SupplyResourceContractLog logTable;

    public SupplyResourceContractLogTest () throws Exception {

        super ();

        jc            = JAXBContext.newInstance (ImportSupplyResourceContractRequest.class);
        schema        = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");

        table         = (SupplyResourceContract) model.get (SupplyResourceContract.class);
        logTable      = (SupplyResourceContractLog) model.get (SupplyResourceContractLog.class);

    }

    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {
            String u = "'" + uuid.toString ().replaceAll ("-", "").toUpperCase () + "'";
            db.d0 (new QP ("UPDATE tb_sr_ctr SET id_log=NULL WHERE uuid = " + u));
            db.d0 (new QP ("DELETE FROM tb_sr_ctr__log WHERE uuid_object = " + u));
        }

    }

    private String createData (final DB db) throws SQLException {

        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);

        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            SupplyResourceContract.c.ID_LOG, id
        ));

        return id;

    }

    Map<String, Object> getData () throws SQLException {

        try (DB db = model.getDb ()) {

            String idLog = createData (db);

            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));
            SupplyResourceContractLog.addFilesForExport(db, r);
            SupplyResourceContractLog.addRefsForExport(db, r);

            return r;

        }

    }

    @Test
    public void test () throws SQLException {

        Map<String, Object> r = getData ();

        dump (r);

        validate (SupplyResourceContractLog.toImportSupplyResourceContractRequest(r));

    }

}
