package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParseSupplyResourceContractsMDBTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("83e1697d-7d09-4aa0-e053-0100007fc05e");

    public ParseSupplyResourceContractsMDBTest () throws Exception {
    }


    @Test (expected = None.class)
    public void test () throws Exception {

        ParseSupplyResourceContractsMDB mdb = new ParseSupplyResourceContractsMDB ();

        try (DB db = model.getDb ()) {

            mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);

            Map<String, Object> r = db.getMap (InXlFile.class, uuid);

            mdb.handleRecord (db, uuid, r);
        }
    }

    @Before
    public void clean() throws SQLException {

	try (DB db = model.getDb()) {
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_svc WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_obj WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_subj WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr WHERE uuid_xl = ?", uuid));
	}

    }
}
