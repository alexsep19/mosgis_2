package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi301;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi302;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.msp.CitizenCompensationOverviewType;
import ru.gosuslugi.dom.schema.integration.msp.ImportCitizenCompensationRequest;
import ru.gosuslugi.dom.schema.integration.msp.ImportCitizenCompensationRequest.ImportCitizenCompensation;

public class CitizenCompensationLog extends GisWsLogTable {

    public CitizenCompensationLog () {

        super (CitizenCompensation.TABLE_NAME + "__log", "История редактирования гражданин, получающих компенсации расходов", CitizenCompensation.class
            , EnTable.c.class
            , CitizenCompensation.c.class
        );
    }
    
    public Get getForExport (String id) {

	final NsiTable nsi95 = NsiTable.getNsiTable(95);

	return (Get) getModel()
                
            .get (this, id, "*")
                
            .toOne (CitizenCompensation.class, "AS r"
		, EnTable.c.UUID.lc()
                , CitizenCompensation.c.ID_CTR_STATUS.lc ()
            ).on ()
                
            .toMaybeOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("r.uuid_org=org.uuid")
                
            .toMaybeOne (VocPerson.class, "AS ind", "*").on ("r.uuid_person=ind.uuid")
            .toMaybeOne (nsi95, nsi95.getLabelField ().getfName () + " AS vc_nsi_95", "code", "guid").on ("(ind.code_vc_nsi_95=vc_nsi_95.code AND vc_nsi_95.isactual=1)")
	;
    }

    public static void addItemsForExport (DB db, Map<String, Object> r) throws SQLException {

	final Model m = db.getModel();

	r.put("decisions", db.getList(m
	    .select(CitizenCompensationDecision.class, "AS root", "*")
	    .toOne(Nsi301.class, "AS vw_nsi_301", "id", "guid").on("root.code_vc_nsi_301 = vw_nsi_301.id")
	    .toMaybeOne(Nsi302.class, "AS vw_nsi_302", "id", "guid").on("root.code_vc_nsi_302 = vw_nsi_302.id")
	    .where(CitizenCompensationDecision.c.UUID_CIT_COMP, r.get("uuid_object"))
	    .and(EnTable.c.IS_DELETED, 0)
	));

	r.put("categories", CitizenCompensationToCategory.selectForExport(db, r.get("r.uuid")));

//	r.put("calcs", db.getList(m
//	    .select(CitizenCompensationCalculation.class, "AS root", "*")
//	    .where(CitizenCompensationCalculation.c.UUID_CIT_COMP, r.get("uuid_object"))
//	    .and(EnTable.c.IS_DELETED, 0)
//	));

	r.put("pays", db.getList(m
	    .select(CitizenCompensationPayment.class, "AS root", "*")
	    .where(CitizenCompensationPayment.c.UUID_CIT_COMP, r.get("uuid_object"))
	    .and(EnTable.c.IS_DELETED, 0)
	));
    }
    
    public static ImportCitizenCompensationRequest toImportCitizenCompensationRequest(Map<String, Object> r) {

	final ImportCitizenCompensationRequest result = new ImportCitizenCompensationRequest();

	result.getImportCitizenCompensation().add(toImportCitizenCompensation(r));

	return result;
    }
    
    public static ImportCitizenCompensationRequest toDeleteCitizenCompensationRequest(Map<String, Object> r) {

	final ImportCitizenCompensationRequest result = new ImportCitizenCompensationRequest();

	result.getImportCitizenCompensation().add(toDeleteCitizenCompensation(r));

	return result;
    }

    public static ImportCitizenCompensation toDeleteCitizenCompensation(Map<String, Object> r) {

	ImportCitizenCompensation result = DB.to.javaBean(ImportCitizenCompensation.class, r);

	result.setCancel(true);

	result.setTransportGuid(UUID.randomUUID().toString());

	return result;
    }

    public static ImportCitizenCompensation toImportCitizenCompensation(Map<String, Object> r) {

	ImportCitizenCompensation result = DB.to.javaBean(ImportCitizenCompensation.class, r);

	result.setLoadOverview(toCitizenCompensationOverview(r));

	result.setTransportGuid(UUID.randomUUID().toString());

//	for (Map<String, Object> i : (List<Map<String, Object>>) r.get("calcs")) {
//	    result.getCalculation().add(CitizenCompensationCalculation.toCitizenCompensationCalculation(i));
//	}

	for (Map<String, Object> i : (List<Map<String, Object>>) r.get("pays")) {
	    result.getPayment().add(CitizenCompensationPayment.toCitizenCompensationPayment(i));
	}

	return result;
    }

    private static CitizenCompensationOverviewType toCitizenCompensationOverview(Map<String, Object> r) {

	Arrays.asList(
	    VocPerson.c.SURNAME.lc(),
	    VocPerson.c.FIRSTNAME.lc(),
	    VocPerson.c.PATRONYMIC.lc(),
	    VocPerson.c.SNILS.lc(),
	    VocPerson.c.PLACEBIRTH.lc()
	).forEach((i) -> {
	    r.put(i, r.get("ind." + i));
	});

	r.put("dateofbirth", r.get(VocPerson.c.BIRTHDATE.lc()));
	r.put("address", r.get(CitizenCompensation.c.FIASHOUSEGUID.lc()));

	switch (DB.to.String(r.get("ind." + VocPerson.c.SEX.lc()))) {
	    case "F":
		r.put("sex", "Female");
		break;
	    case "M":
		r.put("sex", "Male");
		break;
	}

	final CitizenCompensationOverviewType result = DB.to.javaBean(CitizenCompensationOverviewType.class, r);


        Map<String, Object> rr = DB.HASH ();
        
        r.entrySet ().forEach ((kv) -> {
            if (kv.getKey ().startsWith ("ind.")) {
		rr.put(kv.getKey ().substring (4), kv.getValue ());
	    }
        });
        

        if (result.getSNILS () == null && DB.ok (r.get ("ind.code_vc_nsi_95"))) result.setID (toID (r, rr));

	for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("decisions")) {
	    result.getDecision().add (CitizenCompensationDecision.toDecision (i));
	}

	for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("categories")) {
	    result.getCategory().add (CitizenCompensationToCategory.toCitizenCompensationCategory (i));
	}

        return result;
        
    }

    private static ID toID (Map<String, Object> r, Map<String, Object> rr) {
        rr.put ("number", rr.get ("number_"));
        final ID result = DB.to.javaBean (ID.class, rr);
        result.setType (NsiTable.toDom (r, "vc_nsi_95"));
        return result;
    }
}