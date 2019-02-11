package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.LogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;
import ru.gosuslugi.dom.schema.integration.bills.ImportIKUSettlementsRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ContractSubjectType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.bills.ImportRSOSettlementsRequest;
import ru.gosuslugi.dom.schema.integration.bills.ReportPeriodIKUInfoType;
import ru.gosuslugi.dom.schema.integration.bills.ReportPeriodRSOInfoType;
import ru.gosuslugi.dom.schema.integration.bills.ReportPeriodType;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class SettlementDocLog extends GisWsLogTable {

    public SettlementDocLog () {

        super ("tb_st_docs__log", "История редактирования документов расчетов договора ресурсоснабжения", SettlementDoc.class
            , EnTable.c.class
            , SettlementDoc.c.class
        );
    }

    public static ImportRSOSettlementsRequest toImportRSOSettlementsRequest(Map<String, Object> r) {
	final ImportRSOSettlementsRequest createImportRSOSettlementsRequest = new ImportRSOSettlementsRequest();
	final ImportRSOSettlementsRequest.ImportSettlement settlement = toImportSettlement(r);

	final Object ver = r.get(SettlementDoc.c.SETTLEMENTGUID.lc());
	if (ver != null) {
	    settlement.setSettlementGUID(ver.toString());
	}
	createImportRSOSettlementsRequest.getImportSettlement().add(settlement);

	return createImportRSOSettlementsRequest;
    }

    private static ImportRSOSettlementsRequest.ImportSettlement toImportSettlement(Map<String, Object> r) {

	ImportRSOSettlementsRequest.ImportSettlement result = DB.to.javaBean(ImportRSOSettlementsRequest.ImportSettlement.class, r);

	result.setTransportGUID(UUID.randomUUID().toString());

	ImportRSOSettlementsRequest.ImportSettlement.Settlement s = new ImportRSOSettlementsRequest.ImportSettlement.Settlement ();

	ImportRSOSettlementsRequest.ImportSettlement.Settlement.Contract c = new ImportRSOSettlementsRequest.ImportSettlement.Settlement.Contract();
	c.setContractRootGUID(DB.to.String(r.get("sr_ctr.contractrootguid")));
	s.setContract(c);

	List<Map<String, Object>> payments = (List<Map<String, Object>>) r.get("payments");
	if (payments == null) {
	    throw new IllegalStateException("No settlement doc payments fetched: " + r);
	}

	if (!payments.isEmpty()) {

	    for (Map<String, Object> p : payments) {

		p.put("credted", p.get("credited"));

		ImportRSOSettlementsRequest.ImportSettlement.Settlement.ReportingPeriod rp = DB.to.javaBean(ImportRSOSettlementsRequest.ImportSettlement.Settlement.ReportingPeriod.class, p);

		ReportPeriodRSOInfoType info = DB.to.javaBean(ReportPeriodRSOInfoType.class, p);

		if (p.get("debts") == null) {
		    info.setDebts(new BigDecimal(0));
		}

		if (p.get("overpayment") == null) {
		    info.setOverpayment(new BigDecimal(0));
		}

		if (!r.get("sr_ctr.uuid_org_customer").equals(r.get("sr_ctr.uuid_org"))) {
		    info.setPaid(null);
		}

		rp.setReportingPeriodInfo(info);

		s.getReportingPeriod().add(rp);
	    }

	}

	result.setSettlement(s);

	return result;

    }

    public static ImportIKUSettlementsRequest toImportIKUSettlementsRequest(Map<String, Object> r) {
	final ImportIKUSettlementsRequest createImportIKUSettlementsRequest = new ImportIKUSettlementsRequest();
	final ImportIKUSettlementsRequest.ImportSettlement settlement = toImportSettlementUO(r);

	createImportIKUSettlementsRequest.getImportSettlement().add(settlement);

	final Object ver = r.get(SettlementDoc.c.SETTLEMENTGUID.lc());
	if (ver != null) {
	    settlement.setSettlementGUID(ver.toString());
	}

	return createImportIKUSettlementsRequest;
    }

    private static ImportIKUSettlementsRequest.ImportSettlement toImportSettlementUO(Map<String, Object> r) {

	ImportIKUSettlementsRequest.ImportSettlement result = DB.to.javaBean(ImportIKUSettlementsRequest.ImportSettlement.class, r);

	result.setTransportGUID(UUID.randomUUID().toString());

	ImportIKUSettlementsRequest.ImportSettlement.Settlement s = new ImportIKUSettlementsRequest.ImportSettlement.Settlement();

	ImportIKUSettlementsRequest.ImportSettlement.Settlement.Contract c = new ImportIKUSettlementsRequest.ImportSettlement.Settlement.Contract();
	c.setContractRootGUID(DB.to.String(r.get("sr_ctr.contractrootguid")));
	s.setContract(c);

	List<Map<String, Object>> payments = (List<Map<String, Object>>) r.get("payments");
	if (payments == null) {
	    throw new IllegalStateException("No settlement doc payments fetched: " + r);
	}

	if (!payments.isEmpty()) {

	    for (Map<String, Object> p : payments) {

		ImportIKUSettlementsRequest.ImportSettlement.Settlement.ReportingPeriod rp = DB.to.javaBean(ImportIKUSettlementsRequest.ImportSettlement.Settlement.ReportingPeriod.class, p);

		ReportPeriodIKUInfoType info = DB.to.javaBean(ReportPeriodIKUInfoType.class, p);

		rp.setReportingPeriodInfo(info);

		s.getReportingPeriod().add(rp);
	    }

	}

	result.setSettlement(s);

	return result;

    }

    public Get getForExport(Object id) {

	return (Get) getModel()
	    .get(this, id, "*")
	    .toOne(SettlementDoc.class, "AS r",
		SettlementDoc.c.UUID_ORG_AUTHOR.lc(),
		SettlementDoc.c.UUID_SR_CTR.lc(),
		SettlementDoc.c.ID_SD_STATUS.lc(),
		SettlementDoc.c.ID_TYPE.lc()
	    ).on()
	    .toMaybeOne(VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on("r.uuid_org_author=org.uuid")
	    .toOne(SupplyResourceContract.class, "AS sr_ctr",
		SupplyResourceContract.c.CONTRACTROOTGUID.lc(),
		SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc(),
		SupplyResourceContract.c.UUID_ORG.lc()
	    ).on()
	;
    }

    public static void addFilesForExport(DB db, Map<String, Object> r) throws SQLException {
    }

    public static void addItemsForExport(DB db, Map<String, Object> r) throws SQLException {

	final Model m = db.getModel();

	r.put("payments", db.getList(m
	    .select(SettlementDocPayment.class, "AS root", "*")
	    .where(SettlementDocPayment.c.UUID_ST_DOC, r.get("uuid_object"))
	    .and("is_deleted", 0)
	));
    }
}