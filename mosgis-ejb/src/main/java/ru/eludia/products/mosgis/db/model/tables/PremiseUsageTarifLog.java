package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest;
import ru.gosuslugi.dom.schema.integration.tariff.ResidentialPremisesUsageType;
import ru.gosuslugi.dom.schema.integration.tariff.CoefficientType;
import ru.gosuslugi.dom.schema.integration.tariff.DifferentiationType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueEnumerationType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueMultilineType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueOKTMOType;

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
		result.getDifferentiation().add(toDifferentiation(i));
	    }
	}


	return result;
    }

    private static DifferentiationType toDifferentiation(Map<String, Object> i) {

	VocDifferentiationValueKindType.i id_type = VocDifferentiationValueKindType.i.forId(i.get(TarifDiff.c.ID_TYPE.lc()));

	Object op = i.get(TarifDiff.c.OPERATOR.lc());

	VocDifferentiationOperator.i operator = DB.ok(op)? VocDifferentiationOperator.i.forId(op) : null;

	if (!VocDifferentiationValueKindType.i.BOOLEAN.equals(id_type)) {
	    i.put("valueboolean", null);
	}


	boolean is_range = VocDifferentiationOperator.i.RANGE.equals(operator) || VocDifferentiationOperator.i.EXCLUDINGRANGE.equals(operator);

	if (is_range) {
	    String type = id_type.toString().toLowerCase();
	    i.put("value" + type + "1", i.get("value" + type));
	    i.put("value" + type + "2", i.get("value" + type + "_to"));
	    i.put("value" + type, null);
	}

	DifferentiationType result = DB.to.javaBean(DifferentiationType.class, i);

	result.setDifferentiationCode(BigInteger.valueOf((Long)i.get("code_diff")));

	if (DB.ok(operator)) {
	    if (is_range && !VocDifferentiationValueKindType.i.ENUMERATION.equals(id_type)) {
		result.setOperator2(operator.toString());
	    } else {
		switch(id_type){
		    case DATE:
		    case INTEGER:
		    case REAL:
		    case YEAR:
			result.setOperator1(operator.toString());
			break;
		    case ENUMERATION:
			result.setOperator3(operator.toString());
		}
	    }
	}

	switch(id_type){
	    case DATE:
	    case YEAR:
	    case REAL:
	    case INTEGER:
	    case BOOLEAN:
	    case STRING:
		return result;
	    case MULTILINE:
		i.put("name", i.get(TarifDiff.c.LABEL.lc()));
		i.put("value", i.get(TarifDiff.c.VALUESTRING.lc()));
		result.setValueMultiline(DB.to.javaBean(ValueMultilineType.class, i));
		result.setValueString(null);
		return result;
	    case FIAS:
		result.getValueFIAS().addAll((List<String>)i.get("fias"));
		return result;
	    case OKTMO:
		for (String o : (List<String>) i.get("oktmo")) {
		    ValueOKTMOType oktmo = new ValueOKTMOType ();
		    oktmo.setCode(o);
		    result.getValueOKTMO().add(oktmo);
		}
		return result;
	    case ENUMERATION:
		for (Map<String, Object> nsi : (List <Map<String, Object>>) i.get("enumeration")) {
		    ValueEnumerationType val = new ValueEnumerationType();
		    val.setGUID(DB.to.String(nsi.get("enumeration.guid")));
		    val.setCode(DB.to.String(nsi.get("enumeration.code")));
		    result.getValueEnumeration().add(val);
		}
		return result;
	}

	return null;
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

	Select diffsSelect = m
	    .select(TarifDiff.class, "AS root", "*")
	    .where(TarifDiff.c.UUID_TF.lc(), r.get("r.uuid"))
	    .and(EnTable.c.IS_DELETED, 0)
	;

	List <Map<String, Object>> diffs = new ArrayList();

	Map<Object, Map<String, Object>> uuid2diff = new HashMap();

	db.forEach(diffsSelect, (rs) -> {
	    Map<String, Object> i = db.HASH(rs);
	    diffs.add(i);
	    uuid2diff.put(i.get("uuid"), i);

	    VocDifferentiationValueKindType.i id_type = VocDifferentiationValueKindType.i.forId(i.get(TarifDiff.c.ID_TYPE.lc()));

	    if (VocDifferentiationValueKindType.i.FIAS.equals(id_type)) {
		i.put("fias", new ArrayList());
	    }
	    if (VocDifferentiationValueKindType.i.ENUMERATION.equals(id_type)) {
		i.put("enumeration", new ArrayList());
	    }
	    if (VocDifferentiationValueKindType.i.OKTMO.equals(id_type)) {
		i.put("oktmo", new ArrayList());
	    }
	});

	db.forEach(m
	    .select(TarifDiffFias.class, "*")
	    .where("uuid IN", m
		.select(TarifDiff.class, "uuid")
		.where(TarifDiff.c.UUID_TF.lc(), r.get("r.uuid"))
		.and(EnTable.c.IS_DELETED, 0)
	    )
	    , (rs) -> {
		Map<String, Object> i = db.HASH(rs);
		Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		((List<String>)t.get("fias")).add(DB.to.String(i.get("fiashouseguid")));
	    }
	);

	db.forEach(m
	    .select(TarifDiffOktmo.class, "*")
	    .toOne(VocOktmo.class, "AS o", "code").on()
	    .where("uuid IN", m
		.select(TarifDiff.class, "uuid")
		.where(TarifDiff.c.UUID_TF.lc(), r.get("r.uuid"))
		.and(EnTable.c.IS_DELETED, 0)
	    )
	    , (rs) -> {
		Map<String, Object> i = db.HASH(rs);
		Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		((List<String>)t.get("oktmo")).add(DB.to.String(i.get("o.code")));
	    }
	);

	Map<Object, Map<String, Object>> nsi2o = db.getIdx(m
	    .select(TarifDiff.class, "AS root", "*")
	    .toOne(PremiseUsageTarif.class, "AS tf", "*").on("tf.uuid = root.uuid_tf")
	    .toOne(VocDifferentiation.class, "AS vc_diff", "*").where(VocDifferentiation.c.NSIITEM.lc() + " IS NOT NULL").on()
	    .where(TarifDiff.c.UUID_TF.lc(), r.get("r.uuid"))
	    .and(EnTable.c.IS_DELETED, 0)
	    , "vc_diff.nsiitem"
	);

	for (Map.Entry<Object, Map<String, Object>> entry : nsi2o.entrySet()) {

	    Long rnum = DB.to.Long(entry.getKey());

	    Integer registrynumber = rnum.intValue();

	    NsiTable nsiTable = NsiTable.getNsiTable(registrynumber);

	    db.forEach(
		m.select(TarifDiffNsi.class, "AS root", "*")
		    .toOne(nsiTable, "AS enumeration", "code", "guid")
			.where("isactual", 1)
			.on("root.code_vc_nsi = enumeration.code")
		    .where("uuid IN", m
			.select(TarifDiff.class, "uuid")
			.where(TarifDiff.c.UUID_TF.lc(), r.get("r.uuid"))
			.and(EnTable.c.IS_DELETED, 0)
		    )
		, (rs) -> {
		    Map<String, Object> i = db.HASH(rs);
		    Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		    ((List<Map <String, Object>>) t.get("enumeration")).add(i);
		}
	    );
	}

	r.put("diffs", diffs);

	return r;
    }
}