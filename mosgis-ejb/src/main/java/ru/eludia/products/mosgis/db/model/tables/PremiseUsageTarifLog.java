package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest;
import ru.gosuslugi.dom.schema.integration.tariff.ResidentialPremisesUsageType;
import ru.gosuslugi.dom.schema.integration.tariff.CoefficientType;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage.CancelResidentialPremisesUsage;

public class PremiseUsageTarifLog extends GisWsLogTable {

    public PremiseUsageTarifLog () {

        super ("tb_pu_tfs__log", "История редактирования тарифов Размер платы за пользование жилым помещением", PremiseUsageTarif.class
            , EnTable.c.class
            , PremiseUsageTarif.c.class
        );
    }

    public static ImportResidentialPremisesUsageRequest toImportResidentialPremisesUsageRequest(Map<String, Object> r) {

	ImportResidentialPremisesUsageRequest result = new ImportResidentialPremisesUsageRequest();

	ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage t = new ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage();
	t.setTransportGUID(UUID.randomUUID().toString());

	Object ver = r.get(PremiseUsageTarif.c.TARIFFGUID.lc());
	if(ver != null) {
	    t.setTariffGUID(ver.toString());
	}

	t.setLoadResidentialPremisesUsage(toLoadResidentialPremisesUsage (r));
	result.getImportResidentialPremisesUsage().add(t);

	return result;
    }

    public static ImportResidentialPremisesUsageRequest toDeleteResidentialPremisesUsageRequest(Map<String, Object> r) {
	ImportResidentialPremisesUsageRequest result = new ImportResidentialPremisesUsageRequest();

	ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage t = new ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage();
	t.setTransportGUID(UUID.randomUUID().toString());

	Object ver = r.get(PremiseUsageTarif.c.TARIFFGUID.lc());
	if (ver != null) {
	    t.setTariffGUID(ver.toString());
	}

	CancelResidentialPremisesUsage c = DB.to.javaBean(CancelResidentialPremisesUsage.class, r);

	t.setCancelResidentialPremisesUsage(c);

	result.getImportResidentialPremisesUsage().add(t);

	return result;
    }

    private static ResidentialPremisesUsageType toLoadResidentialPremisesUsage(Map<String, Object> r) {

	ResidentialPremisesUsageType result = DB.to.javaBean(ResidentialPremisesUsageType.class, r);

	List<Map<String, Object>> oktmos = (List<Map<String, Object>>) r.get("oktmos");
	for (Map<String, Object> o : oktmos) {
	    result.getOKTMO().add(VocOktmo.createOKTMORef(DB.to.Long(o.get("code"))));
	}

	List<Map<String, Object>> legal_acts = (List<Map<String, Object>>) r.get("legal_acts");
	for (Map<String, Object> la : legal_acts) {
	    String documentguid = la.get(LegalAct.c.DOCUMENTGUID.lc()).toString();
	    result.getActGUID().add(documentguid);
	}

	List<Map<String, Object>> coeffs = (List<Map<String, Object>>) r.get("coeffs");
	if (coeffs.isEmpty()) {
	    result.setCoefficientsNotSet(true);
	} else {
	    for (Map<String, Object> i : coeffs) {
		CoefficientType c = DB.to.javaBean(CoefficientType.class, i);
		result.getCoefficient().add(c);
	    }
	}

	List<Map<String, Object>> diffs = (List<Map<String, Object>>) r.get("diffs");
	if (diffs.isEmpty()) {
	    result.setDifferentiationNotSet(true);
	} else {
	    for (Map<String, Object> i : diffs) {
		result.getDifferentiation().add(TarifDiff.toDifferentiation(i));
	    }
	}

	return result;
    }

    public static Map<String, Object> getForExport(DB db, String id) throws SQLException {

	final Model m = db.getModel();

	final Map<String, Object> r = db.getMap(m
	    .get(PremiseUsageTarifLog.class, id, "*")
	    .toOne(PremiseUsageTarif.class, "AS r",
		 EnTable.c.UUID.lc(),
		 PremiseUsageTarif.c.ID_CTR_STATUS.lc()
	    ).on()
	    .toOne(VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on("r.uuid_org=org.uuid")
	);

	r.put("oktmos", db.getList(m
	    .select(VocOktmo.class, "*")
	    .where(VocOktmo.c.ID.lc() + " IN", m.select(PremiseUsageTarifOktmo.class, "oktmo")
		.where("uuid", r.get("r.uuid"))
	    )
	));
	r.put("legal_acts", db.getList(m
	    .select(LegalAct.class, LegalAct.c.DOCUMENTGUID.lc())
	    .where(EnTable.c.UUID.lc() + " IN", m.select(TarifLegalAct.class, "uuid_legal_act")
		.where("uuid", r.get("r.uuid"))
	    )
	));

	r.put("coeffs", db.getList(m
	    .select(TarifCoeff.class, "*")
	    .where(TarifCoeff.c.UUID_TF.lc(), r.get("r.uuid"))
	    .and(EnTable.c.IS_DELETED, 0)
	));

	r.put("diffs", TarifDiff.getForExportDifferentiation(db, DB.to.String(r.get("r.uuid"))));

	return r;
    }
}