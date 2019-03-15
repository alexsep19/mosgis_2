package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
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
    
    private static final UUID uuid = UUID.fromString ("840a696e-3a81-672e-e053-0d0b000aa429");

    private static final Logger logger = Logger.getLogger(ParseSupplyResourceContractsMDBTest.class.getName());

    public ParseSupplyResourceContractsMDBTest () throws Exception {
    }

    @Test (expected = None.class)
    public void testParse () throws Exception {

        ParseSupplyResourceContractsMDB mdb = new ParseSupplyResourceContractsMDB ();

        try (DB db = model.getDb ()) {

	    mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);

            Map<String, Object> r = db.getMap (InXlFile.class, uuid);

            mdb.handleRecord (db, uuid, r);
        }
    }

    @Ignore
    @Test(expected = None.class)
    public void killXlSupplyResourceContracts() throws Exception {

	ParseSupplyResourceContractsMDB mdb = new ParseSupplyResourceContractsMDB();

	try (DB db = model.getDb()) {

	    db.forEach(model.select(SupplyResourceContract.class, "AS root", "uuid", "contractnumber", "uuid_xl")
		.where("contractnumber LIKE", "Шаблон %"),
		 (rs) -> {
		    Map<String, Object> ctr = db.HASH(rs);

		    logger.info(DB.to.json(ctr).toString());

		    try {
			mdb.killXlSupplyResourceContract(db, (UUID) ctr.get("uuid_xl"));
		    } catch (SQLException ex) {
		    }
		}
	    );
	}
    }

    @Before
    public void clean() throws SQLException {

	try (DB db = model.getDb()) {
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_qls WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_svc WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_obj WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr_subj WHERE uuid_xl = ?", uuid));
	    db.d0(new QP("DELETE FROM in_xl_sr_ctr WHERE uuid_xl = ?", uuid));
	}

    }
}
