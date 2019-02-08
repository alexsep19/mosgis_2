package ru.eludia.products.mosgis.db.model.tables;

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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractSubjectType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.ContractSubject;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.ObjectAddress;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class SupplyResourceContractLog extends GisWsLogTable {

    public SupplyResourceContractLog () {

        super ("tb_sr_ctr__log", "История редактирования договоров ресурсоснабжения", SupplyResourceContract.class
            , EnTable.c.class
            , SupplyResourceContract.c.class
        );
    }

    public static ImportSupplyResourceContractRequest toImportSupplyResourceContractRequest(Map<String, Object> r) {
	final ImportSupplyResourceContractRequest createImportSupplyResourceContractRequest = new ImportSupplyResourceContractRequest();
	final ImportSupplyResourceContractRequest.Contract contract = new ImportSupplyResourceContractRequest.Contract();
	final SupplyResourceContractType supplyResourceContract = toContractSupplyResourceContract(r);
	contract.setSupplyResourceContract(supplyResourceContract);
	contract.setTransportGUID(UUID.randomUUID().toString());
	final Object ver = r.get(SupplyResourceContract.c.CONTRACTGUID.lc());
	if (ver != null) {
	    contract.setContractGUID(ver.toString());
	}
	createImportSupplyResourceContractRequest.getContract().add(contract);
	return createImportSupplyResourceContractRequest;
    }

    private static SupplyResourceContractType toContractSupplyResourceContract(Map<String, Object> r) {

	r.put("automaticrolloveroneyear", r.get("autorollover"));
	r.put("specifyingqualityindicators", r.get("specqtyinds"));
	r.put("meteringdeviceinformation", r.get("mdinfo"));
	r.put("comptetiondate", r.get("completiondate"));
	r.put("specifyingqualityindicators", r.get("voc_specqtyinds.name"));
	r.put("plannedvolumetype", r.get("voc_plannedvolumetype.name"));
	r.put("accrualprocedure", r.get("voc_accrualprocedure.name"));
	if (r.get("countingresource") != null) {
	    r.put("countingresource", Boolean.TRUE.equals(r.get("countingresource")) ? "R" : "P");
	}

	SupplyResourceContractType result = DB.to.javaBean(SupplyResourceContractType.class, r);

	if (!Boolean.TRUE.equals(result.isIsPlannedVolume())) {
	    result.setIsPlannedVolume(Boolean.FALSE);
	    result.setPlannedVolumeType(null);
	}

	if (Boolean.TRUE.equals(r.get("is_contract"))) {
	    SupplyResourceContractType.IsContract is_c = DB.to.javaBean(SupplyResourceContractType.IsContract.class, r);
	    result.setIsContract(is_c);
	} else {
	    SupplyResourceContractType.IsNotContract is_c = DB.to.javaBean(SupplyResourceContractType.IsNotContract.class, r);
	    result.setIsNotContract(is_c);
	}

	result.getContractBase().add(NsiTable.toDom(r, "vc_nsi_58"));

	if (Boolean.TRUE.equals(result.isVolumeDepends()) || Boolean.TRUE.equals(result.isMeteringDeviceInformation())) {
	    result.setPeriod(toPeriod(r));
	}

	VocGisSupplyResourceContractCustomerType.i id_customer_type =
	    VocGisSupplyResourceContractCustomerType.i.forId(r.get("ctr." + SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc()))
	;

	if (id_customer_type != VocGisSupplyResourceContractCustomerType.i.ORGANIZATION) {
	    result.setAccrualProcedure(null);
	}

	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.OWNER) {
	    result.setApartmentBuildingOwner(toApartmentBuildingOwner(r));
	}
	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.REPRESENTATIVEOWNER) {
	    result.setApartmentBuildingRepresentativeOwner(toApartmentBuildingRepresentativeOwner (r));
	}
	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.SOLEOWNER) {
	    result.setApartmentBuildingSoleOwner (toApartmentBuildingSoleOwner (r));
	}
	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER) {
	    result.setLivingHouseOwner (toLivingHouseOwner (r));
	}
	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.ORGANIZATION) {
	    SupplyResourceContractType.Organization org = new SupplyResourceContractType.Organization();
	    org.setOrgRootEntityGUID(r.get("ctr.uuid_org_customer").toString());
	    result.setOrganization(org);
	}
	if (id_customer_type == VocGisSupplyResourceContractCustomerType.i.OFFER) {
	    result.setOffer(Boolean.TRUE);
	}

	result.setBillingDate(toBillingDate(r));
	result.setPaymentDate(toPaymentDate(r));
	result.setProvidingInformationDate(toProvidingInformationDate(r));

	List<Map<String, Object>> files = (List<Map<String, Object>>) r.get("files");
	if (files == null) {
	    throw new IllegalStateException("No files fetched: " + r);
	}

	for (Map<String, Object> file : files) {

	    switch (VocSupplyResourceContractFileType.i.forId(DB.to.Long(file.get("id_type")))) {
	    case ADDENDUM:
	    case OTHER:
	    case CONTRACT:
		if(result.getIsContract() != null) {
		    result.getIsContract().getContractAttachment().add(AttachTable.toAttachmentType(file));
		} else {
		    result.getIsNotContract().getContractAttachment().add(AttachTable.toAttachmentType(file));
		}
		break;
	    default:
		throw new IllegalStateException("Invalid file type: " + r);
	    }

	}

	List<Map<String, Object>> subjects = (List<Map<String, Object>>) r.get("subjects");
	if (subjects == null) {
	    throw new IllegalStateException("No supply resource contract subjects fetched: " + r);
	}

	if (!subjects.isEmpty()) {

	    boolean plannedVolumeInSubjects = result.isIsPlannedVolume()
		&& result.getPlannedVolumeType().equals(VocGisContractDimension.i.BY_CONTRACT.getName());

	    for (Map<String, Object> s : subjects) {
		ContractSubject cs = DB.to.javaBean(ContractSubject.class, s);
		cs.setTransportGUID(s.get("uuid").toString());

		ContractSubjectType.ServiceType st = new ContractSubjectType.ServiceType();
		NsiRef nsi_st = NsiTable.toDom(s, "vc_nsi_3");
		st.setCode(nsi_st.getCode());
		st.setGUID(nsi_st.getGUID());
		cs.setServiceType(st);

		ContractSubjectType.MunicipalResource mr = new ContractSubjectType.MunicipalResource();
		NsiRef nsi_mr = NsiTable.toDom(s, "vc_nsi_239");
		mr.setCode(nsi_mr.getCode());
		mr.setGUID(nsi_mr.getGUID());
		cs.setMunicipalResource(mr);

		if (plannedVolumeInSubjects) {
		    ContractSubjectType.PlannedVolume volume = DB.to.javaBean(ContractSubjectType.PlannedVolume.class, s);
		    cs.setPlannedVolume(volume);
		}

		result.getContractSubject().add(cs);
	    }
	    
	}

	List<Map<String, Object>> objects = (List<Map<String, Object>>) r.get("objects");
	if (objects == null) {
	    throw new IllegalStateException("No supply resource contract objects fetched: " + r);
	}

	if (!objects.isEmpty()) {

	    boolean plannedVolumeInObjects = result.isIsPlannedVolume()
		&& result.getPlannedVolumeType().equals(VocGisContractDimension.i.BY_HOUSE.getName());

	    for (Map<String, Object> o : objects) {
		o.put("apartmentnumber", o.get("premise.apartmentnumber"));
		o.put("roomnumber", o.get("premise.roomnumber"));

		ObjectAddress oa = DB.to.javaBean(ObjectAddress.class, o);

		oa.setTransportGUID(o.get("uuid").toString());

		List<Map<String, Object>> services = (List<Map<String, Object>>) o.get("services");

		for (Map<String, Object> service : services) {

		    service.put("pairkey", service.get("pairkey.uuid"));

		    ObjectAddress.Pair pair = DB.to.javaBean(ObjectAddress.Pair.class, service);

		    ObjectAddress.Pair.HeatingSystemType hs = new ObjectAddress.Pair.HeatingSystemType ();

		    hs.setOpenOrNot(Boolean.TRUE.equals(service.get("is_heat_open")) ? "Opened" : "Closed");
		    hs.setCentralizedOrNot(Boolean.TRUE.equals(service.get("is_heat_centralized")) ? "Centralized" : "Decentralized");

		    pair.setHeatingSystemType(hs);

		    oa.getPair().add(pair);

		    if (plannedVolumeInObjects) {
			ObjectAddress.PlannedVolume v = DB.to.javaBean(ObjectAddress.PlannedVolume.class, service);
			oa.getPlannedVolume().add(v);
		    }
		}

		result.getObjectAddress().add(oa);
	    }

	}

	// TODO: result.getQuality().add(quality)
	// TODO: result.getOtherQualityIndicator().add(oquality)
	// TODO: result.getTemperatureChart().add(tc)

	return result;

    }

    private static DRSOIndType toDRSOIndType(Map<String, Object> r){

	DRSOIndType ind = new DRSOIndType();

	if (DB.ok(r.get("vc_nsi_95.code"))) {

	    r.put("surname", r.get("p.surname"));
	    r.put("firstname", r.get("p.firstname"));
	    r.put("number", r.get("p.number_"));
	    r.put("series", r.get("p.series"));
	    r.put("issuedate", r.get("p.issuedate"));

	    ind = DB.to.javaBean(DRSOIndType.class, r);
	    ind.setID(toID(r));
	    ind.setSNILS(null);
	} else {
	    ind.setSNILS(DB.to.String(r.get("p.snils")));
	}

	return ind;
    }

    private static SupplyResourceContractType.ApartmentBuildingOwner toApartmentBuildingOwner(Map<String, Object> r) {

	SupplyResourceContractType.ApartmentBuildingOwner o = new SupplyResourceContractType.ApartmentBuildingOwner();

	UUID uuidOrg = (UUID)r.get("ctr.uuid_org_customer");
	UUID uuidPerson = (UUID)r.get("ctr.uuid_person_customer");

	if(uuidOrg == null && uuidPerson == null) {
	    o.setNoData(Boolean.TRUE);
	    return o;
	}

	if (uuidOrg != null) {
	    o.setRegOrg(VocOrganization.dRSORegOrgType(uuidOrg));
	    return o;
	}

	if (uuidPerson != null) {
	    o.setInd(toDRSOIndType(r));
	    return o;
	}

	return o;
    }

    private static SupplyResourceContractType.ApartmentBuildingRepresentativeOwner toApartmentBuildingRepresentativeOwner(Map<String, Object> r) {

	SupplyResourceContractType.ApartmentBuildingRepresentativeOwner o = new SupplyResourceContractType.ApartmentBuildingRepresentativeOwner();
	UUID uuidOrg = (UUID) r.get("ctr.uuid_org_customer");
	UUID uuidPerson = (UUID) r.get("ctr.uuid_person_customer");

	if (uuidOrg == null && uuidPerson == null) {
	    o.setNoData(Boolean.TRUE);
	    return o;
	}

	if (uuidOrg != null) {
	    o.setRegOrg(VocOrganization.dRSORegOrgType(uuidOrg));
	    return o;
	}

	if (uuidPerson != null) {
	    o.setInd(toDRSOIndType(r));
	    return o;
	}

	return o;
    }

    private static SupplyResourceContractType.ApartmentBuildingSoleOwner toApartmentBuildingSoleOwner(Map<String, Object> r) {

	SupplyResourceContractType.ApartmentBuildingSoleOwner o = new SupplyResourceContractType.ApartmentBuildingSoleOwner();
	UUID uuidOrg = (UUID) r.get("ctr.uuid_org_customer");
	UUID uuidPerson = (UUID) r.get("ctr.uuid_person_customer");

	if (uuidOrg == null && uuidPerson == null) {
	    o.setNoData(Boolean.TRUE);
	    return o;
	}

	if (uuidOrg != null) {
	    o.setRegOrg(VocOrganization.dRSORegOrgType(uuidOrg));
	    return o;
	}

	if (uuidPerson != null) {
	    o.setInd(toDRSOIndType(r));
	    return o;
	}

	return o;
    }

    private static SupplyResourceContractType.LivingHouseOwner toLivingHouseOwner(Map<String, Object> r) {

	SupplyResourceContractType.LivingHouseOwner o = new SupplyResourceContractType.LivingHouseOwner();
	UUID uuidOrg = (UUID) r.get("ctr.uuid_org_customer");
	UUID uuidPerson = (UUID) r.get("ctr.uuid_person_customer");

	if (uuidOrg == null && uuidPerson == null) {
	    o.setNoData(Boolean.TRUE);
	    return o;
	}

	if (uuidOrg != null) {
	    o.setRegOrg(VocOrganization.dRSORegOrgType(uuidOrg));
	    return o;
	}

	if (uuidPerson != null) {
	    o.setInd(toDRSOIndType(r));
	    return o;
	}

	return o;
    }

    private static ID toID(Map<String, Object> r) {
	final ID id = DB.to.javaBean(ID.class, r);
	id.setType(NsiTable.toDom(r, "vc_nsi_95"));
	return id;
    }

    private static SupplyResourceContractType.Period toPeriod(Map<String, Object> r) {

	final SupplyResourceContractType.Period result = DB.to.javaBean(SupplyResourceContractType.Period.class, r);


	final SupplyResourceContractType.Period.Start start = new SupplyResourceContractType.Period.Start();
	String f = SupplyResourceContract.c.DDT_M_START.lc();
	if(r.get(f) == null) {
	    return null;
	}
	byte d = Byte.parseByte(DB.to.String(r.get(f)));
	if (d == 32) {
	    start.setNextMonth(true);
	} else {
	    start.setNextMonth(null);
	    start.setStartDate(d);
	}
	start.setNextMonth(DB.ok(r.get(f + "_nxt")));

	final SupplyResourceContractType.Period.End   end = new SupplyResourceContractType.Period.End();
	f = SupplyResourceContract.c.DDT_M_END.lc();
	if (r.get(f) == null) {
	    return null;
	}
	d = Byte.parseByte(DB.to.String(r.get(f)));
	if (d == 32) {
	    end.setNextMonth(true);
	} else {
	    end.setNextMonth(null);
	    end.setEndDate(d);
	}
	end.setNextMonth(DB.ok(r.get(f + "_nxt")));


	result.setStart(start);
	result.setEnd(end);

	return result;
    }

    private static SupplyResourceContractType.BillingDate toBillingDate(Map<String, Object> r) {

	final SupplyResourceContractType.BillingDate result = DB.to.javaBean(SupplyResourceContractType.BillingDate.class, r);

	String f = SupplyResourceContract.c.DDT_D_START.lc();

	if (r.get(f) == null) {
	    return null;
	}

	byte d = Byte.parseByte(DB.to.String(r.get(f)));
	if (d == 99) {
	    d = -1;
	}

	if (DB.ok(r.get(f + "_nxt"))) {
	    result.setDateType("N");
	} else {
	    result.setDateType("C");
	}
	result.setDate(d);

	return result;
    }

    private static SupplyResourceContractType.PaymentDate toPaymentDate(Map<String, Object> r) {

	final SupplyResourceContractType.PaymentDate result = DB.to.javaBean(SupplyResourceContractType.PaymentDate.class, r);

	String f = SupplyResourceContract.c.DDT_I_START.lc();

	if (r.get(f) == null) {
	    return null;
	}

	byte d = Byte.parseByte(DB.to.String(r.get(f)));
	if (d == 99) {
	    d = -1;
	}

	if (DB.ok(r.get(f + "_nxt"))) {
	    result.setDateType("N");
	} else {
	    result.setDateType("C");
	}
	result.setDate(d);

	return result;
    }

    private static SupplyResourceContractType.ProvidingInformationDate toProvidingInformationDate(Map<String, Object> r) {

	final SupplyResourceContractType.ProvidingInformationDate result = DB.to.javaBean(SupplyResourceContractType.ProvidingInformationDate.class, r);

	String f = SupplyResourceContract.c.DDT_N_START.lc();

	if(r.get(f) == null){
	    return null;
	}

	byte d = Byte.parseByte(DB.to.String(r.get(f)));
	if (d == 99) {
	    d = -1;
	}

	if (DB.ok(r.get(f + "_nxt"))) {
	    result.setDateType("N");
	} else {
	    result.setDateType("C");
	}
	result.setDate(d);

	return result;
    }

    public Get getForExport(Object id) {

	NsiTable nsi58 = NsiTable.getNsiTable(58);
	NsiTable nsi95 = NsiTable.getNsiTable(95);

	return (Get) getModel()
	    .get(this, id, "*")
	    .toOne(SupplyResourceContract.class, "AS ctr",
		EnTable.c.UUID.lc(),
		SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc(),
		SupplyResourceContract.c.UUID_PERSON_CUSTOMER.lc(),
		SupplyResourceContract.c.ID_CTR_STATUS.lc(),
		SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc()
	    ).on()
	    .toMaybeOne(nsi58, "AS vc_nsi_58", "code", "guid").on("ctr.code_vc_nsi_58=vc_nsi_58.code AND vc_nsi_58.isactual = 1")
	    .toMaybeOne(VocGisContractDimension.class, "AS voc_plannedvolumetype", "name").on("ctr.plannedvolumetype = voc_plannedvolumetype.id")
	    .toMaybeOne(VocGisContractDimension.class, "AS voc_specqtyinds", "name").on("ctr.specqtyinds = voc_specqtyinds.id")
	    .toMaybeOne(VocGisContractDimension.class, "AS voc_accrualprocedure", "name").on("ctr.accrualprocedure = voc_accrualprocedure.id")
	    .toMaybeOne(VocOrganization.class, "AS org", "orgppaguid").on("ctr.uuid_org=org.uuid")
	    .toMaybeOne(VocPerson.class, "AS p", "*").on("ctr.uuid_person_customer=p.uuid")
	    .toMaybeOne(nsi95, "AS vc_nsi_95", "*").on("p.code_vc_nsi_95=vc_nsi_95.code")
	    .toMaybeOne(VocOrganization.class, "AS o", "*").on("ctr.uuid_org_customer=o.uuid")
	;
    }

    public static void addFilesForExport(DB db, Map<String, Object> r) throws SQLException {

	r.put("files", db.getList(db.getModel()
	    .select(SupplyResourceContractFile.class, "*")
	    .where(SupplyResourceContractFile.c.UUID_SR_CTR.lc(), r.get("uuid_object"))
	    .toOne(SupplyResourceContractFileLog.class, "AS log", "ts_start_sending", "err_text").on()
	    .and("id_status", 1)
	));

    }

    public static void addRefsForExport(DB db, Map<String, Object> r) throws SQLException {

	final Model m = db.getModel();

	NsiTable nsi3 = NsiTable.getNsiTable(3);

	r.put("subjects", db.getList(m
	    .select(SupplyResourceContractSubject.class, "AS root", "*")
	    .toOne(nsi3, "AS vc_nsi_3", "code", "guid").on("root.code_vc_nsi_3=vc_nsi_3.code AND vc_nsi_3.isactual = 1")
	    .toOne(VocNsi239.class, "AS vc_nsi_239", "code", "guid").on("root.code_vc_nsi_239=vc_nsi_239.code AND vc_nsi_239.isactual = 1")
	    .where(SupplyResourceContractSubject.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IS NULL")
	    .and("is_deleted", 0)
	));

	Map<UUID, Map<String, Object>> id2o = new HashMap<>();

	Select objects = m
	    .select(SupplyResourceContractObject.class, "AS root", "*")
	    .toMaybeOne(Premise.class, "AS premise", "apartmentnumber", "roomnumber").on()
	    .where(SupplyResourceContractObject.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and("is_deleted", 0)
	;

	db.forEach(objects, (rs) -> {
		Map<String, Object> object = db.HASH(rs);
		object.put("services", new ArrayList());
		id2o.put((UUID)object.get("uuid"), object);
	    }
	);

	r.put("objects", new ArrayList<Map<String, Object>> (id2o.values()));

	db.forEach(m.select(SupplyResourceContractSubject.class, "AS root", "*")
	    .where(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IN"
		, m.select(SupplyResourceContractObject.class, "uuid")
		    .where(SupplyResourceContractObject.c.UUID_SR_CTR, r.get("ctr.uuid"))
		    .and("is_deleted", 0)
	    )
	    .toOne(SupplyResourceContractSubject.class, "AS pairkey", "uuid")
		.on("pairkey.uuid_sr_ctr = root.uuid_sr_ctr "
		    + " AND pairkey.is_deleted = 0 "
		    + " AND pairkey.uuid_sr_ctr_obj IS NULL "
		    + " AND root.code_vc_nsi_3=pairkey.code_vc_nsi_3 AND root.code_vc_nsi_239=pairkey.code_vc_nsi_239 "
		    + " AND (root.startsupplydate <= pairkey.endsupplydate OR pairkey.endsupplydate IS NULL) "
		    + " AND (root.endsupplydate >= pairkey.startsupplydate OR root.endsupplydate IS NULL) "
		)
	    .and("is_deleted", 0)
	    , (rs) -> {
		Map<String, Object> subject = db.HASH(rs);

		final Map<String, Object> o = id2o.get(subject.get("uuid_sr_ctr_obj"));

		if (o != null) {
		    ((List) o.get("services")).add(subject);
		}
	    }
	);
    }
}