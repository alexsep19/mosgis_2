package ru.eludia.products.mosgis.db.model.tables;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest.ImportAccountRegOperator;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest.ImportAccountRegOperator.LoadAccountRegOperator;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest.ImportAccountRegOperator.CloseAccountRegOperator;

public class BankAccountLog extends GisWsLogTable {

    public BankAccountLog () {

        super ("tb_bnk_accts__log", "История редактирования платёжных реквизитов (расчётных счетов)", BankAccount.class
            , EnTable.c.class
            , BankAccount.c.class
        );

    }

    public Get getForExport (String id) {

	return (Get) getModel()
                
            .get (this, id, "*")
                
            .toOne (BankAccount.class, "AS r"
		, EnTable.c.UUID.lc()
                , BankAccount.c.ID_CTR_STATUS.lc ()
            ).on ()
                
            .toMaybeOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("r.uuid_org=org.uuid")
	    .toMaybeOne(VocOrganization.class, "AS cred_org", "uuid", "label").on("r.uuid_cred_org = cred_org.uuid")
	;
    }

    public static ImportAccountRegionalOperatorRequest toImportAccountRegionalOperatorRequest(Map<String, Object> r) {

	final ImportAccountRegionalOperatorRequest result = new ImportAccountRegionalOperatorRequest();

	result.getImportAccountRegOperator().add(toImportAccountRegOperator(r));

	return result;
    }

    private static ImportAccountRegOperator toImportAccountRegOperator(Map<String, Object> r) {

	ImportAccountRegOperator result = DB.to.javaBean(ImportAccountRegOperator.class, r);

	result.setTransportGuid(UUID.randomUUID().toString());

	result.setLoadAccountRegOperator(toLoadAccountRegOperator (r));

	return result;
    }

    private static LoadAccountRegOperator toLoadAccountRegOperator(Map<String, Object> r) {

	r.put("number", r.get(BankAccount.c.ACCOUNTNUMBER.lc()));

	if (DB.ok(r.get(BankAccount.c.ACCOUNTREGOPERATORGUID.lc()))) {

	    String ts_plus_1_day = LocalDate.parse(r.get("ts").toString().substring(0, 10)).plusDays(1).toString();

	    r.put(BankAccount.c.OPENDATE.lc(), ts_plus_1_day); // HACK: opendate / update date should be greater by 1 day each time
	}

	LoadAccountRegOperator result = DB.to.javaBean(LoadAccountRegOperator.class, r);

	result.setCredOrganization(VocOrganization.regOrgType((UUID)r.get("cred_org.uuid")));

	return result;
    }

    public static ImportAccountRegionalOperatorRequest toTerminateAccountRegionalOperatorRequest(Map<String, Object> r) {

	final ImportAccountRegOperator iao = DB.to.javaBean(ImportAccountRegOperator.class, r);

	iao.setTransportGuid(UUID.randomUUID().toString());

	iao.setCloseAccountRegOperator(DB.to.javaBean(CloseAccountRegOperator.class, r));


	final ImportAccountRegionalOperatorRequest result = new ImportAccountRegionalOperatorRequest();

	result.getImportAccountRegOperator().add(iao);


	return result;
    }

}