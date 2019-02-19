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

    private SupplyResourceContract table;
    private SupplyResourceContractLog logTable;

    public SupplyResourceContractLogTest () throws Exception {

        super ();

        jc            = JAXBContext.newInstance (ImportSupplyResourceContractRequest.class);
        schema        = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");

        table         = (SupplyResourceContract) model.get (SupplyResourceContract.class);
        logTable      = (SupplyResourceContractLog) model.get (SupplyResourceContractLog.class);

    }

    private Map<String, Object> getData() throws SQLException {

	try (DB db = model.getDb()) {
	    final Map<String, Object> r = db.getMap(logTable.getForExport("823e2617-bc36-4766-e053-0100007f0f12"));
	    r.put("contractrootguid", r.get("uuid"));
	    r.put("contractguid", "0000000000000000");
	    SupplyResourceContractLog.addFilesForExport(db, r);
	    SupplyResourceContractLog.addRefsForExport(db, r);
	    return r;
	}
    }

    @Test(expected = Test.None.class)
    public void testInsert() throws SQLException {

	Map<String, Object> r = getData();

	r.put("contractrootguid", null);
	r.put("contractguid", "0000000000000000");
	dump(r);
	validate(SupplyResourceContractLog.toImportSupplyResourceContractRequest(r));

    }

    @Test(expected = Test.None.class)
    public void testUpdate() throws SQLException {

	Map<String, Object> r = getData();

	r.put("contractrootguid", r.get("uuid"));
	r.put("contractguid", "0000000000000000");
	dump(r);
	validate(SupplyResourceContractLog.toImportSupplyResourceContractRequest(r));

    }

    @Test(expected = Test.None.class)
    public void testTerminate() throws SQLException {

	Map<String, Object> r = getData();

	r.put("contractrootguid", r.get("uuid"));
	r.put("contractguid", "0000000000000000");
	dump(r);
	validate(SupplyResourceContractLog.toTerminateSupplyResourceContractRequest(r));

    }
}
