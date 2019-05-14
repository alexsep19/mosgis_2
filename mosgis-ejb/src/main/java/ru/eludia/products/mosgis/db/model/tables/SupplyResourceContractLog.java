package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi3;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi276;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi239;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.model.Type;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractQualityLevelType;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;
import ru.gosuslugi.dom.schema.integration.house_management.AnnulmentType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractSubjectType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.ContractSubject;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.ObjectAddress;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.Quality;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.OtherQualityIndicator;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType.TemperatureChart;
import ru.gosuslugi.dom.schema.integration.house_management.TerminateType;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class SupplyResourceContractLog extends GisWsLogTable {

    public SupplyResourceContractLog () {

        super ("tb_sr_ctr__log", "История редактирования договоров ресурсоснабжения", SupplyResourceContract.class
            , EnTable.c.class
            , SupplyResourceContract.c.class
        );

	fk    ("uuid_vc_org_log", VocOrganizationLog.class,         null,     "Родительское событие истории организации");

	col   ("exportobjectguid", Type.UUID, null, "Идентификатор следующей страницы для события массового импорта ДРСО из ГИС ЖКХ");
    }

    public static ImportSupplyResourceContractRequest toImportSupplyResourceContractRequest(Map<String, Object> r) {
	final ImportSupplyResourceContractRequest createImportSupplyResourceContractRequest = new ImportSupplyResourceContractRequest();
	final ImportSupplyResourceContractRequest.Contract contract = new ImportSupplyResourceContractRequest.Contract();
	final SupplyResourceContractType supplyResourceContract = toContractSupplyResourceContract(r);
	contract.setSupplyResourceContract(supplyResourceContract);
	contract.setTransportGUID(UUID.randomUUID().toString());
	final Object ver = r.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc());
	if (ver != null) {
	    contract.setContractRootGUID(ver.toString());
	}
	createImportSupplyResourceContractRequest.getContract().add(contract);
	return createImportSupplyResourceContractRequest;
    }

    public static ImportSupplyResourceContractRequest toAnnulSupplyResourceContractRequest(Map<String, Object> r) {
	final ImportSupplyResourceContractRequest annulRequest = new ImportSupplyResourceContractRequest();
	final ImportSupplyResourceContractRequest.Contract contract = new ImportSupplyResourceContractRequest.Contract();
	final AnnulmentType annulmentContract = DB.to.javaBean(AnnulmentType.class, r);
	contract.setAnnulmentContract(annulmentContract);
	contract.setTransportGUID(UUID.randomUUID().toString());
	final Object ver = r.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc());
	if (ver != null) {
	    contract.setContractRootGUID(ver.toString());
	}
	annulRequest.getContract().add(contract);
	return annulRequest;
    }

    public static ImportSupplyResourceContractRequest toTerminateSupplyResourceContractRequest(Map<String, Object> r) {
	final ImportSupplyResourceContractRequest terminateRequest = new ImportSupplyResourceContractRequest();
	final ImportSupplyResourceContractRequest.Contract contract = new ImportSupplyResourceContractRequest.Contract();

	final ImportSupplyResourceContractRequest.Contract.TerminateContract tc = DB.to.javaBean(ImportSupplyResourceContractRequest.Contract.TerminateContract.class, r);
	tc.setReasonRef(NsiTable.toDom(r.get("code_vc_nsi_54").toString(), (UUID) r.get("vc_nsi_54.guid")));
	contract.setTerminateContract(tc);

	contract.setTransportGUID(UUID.randomUUID().toString());
	final Object ver = r.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc());
	if (ver != null) {
	    contract.setContractRootGUID(ver.toString());
	}

	terminateRequest.getContract().add(contract);

	return terminateRequest;
    }

    private static SupplyResourceContractType toContractSupplyResourceContract(Map<String, Object> r) {

	r.put("specifyingqualityindicators", r.get("specqtyinds"));
	r.put("meteringdeviceinformation", r.get("mdinfo"));
	r.put("comptetiondate", r.get("completiondate"));
	r.put("specifyingqualityindicators", r.get("voc_specqtyinds.name"));
	r.put("plannedvolumetype", r.get("voc_plannedvolumetype.name"));
	r.put("accrualprocedure", r.get("voc_accrualprocedure.name"));
	if (r.get("countingresource") != null) {
	    r.put("countingresource", DB.ok(r.get("countingresource")) ? "R" : "P");
	}
	if (DB.ok(r.get("autorollover"))) {
	    r.put("automaticrolloveroneyear", r.get("autorollover"));
	}

	SupplyResourceContractType result = DB.to.javaBean(SupplyResourceContractType.class, r);

	if (!DB.ok(result.isIsPlannedVolume())) {
	    result.setIsPlannedVolume(Boolean.FALSE);
	    result.setPlannedVolumeType(null);
	}

	if (DB.ok(r.get("is_contract"))) {
	    SupplyResourceContractType.IsContract is_c = DB.to.javaBean(SupplyResourceContractType.IsContract.class, r);
	    result.setIsContract(is_c);
	} else {
	    SupplyResourceContractType.IsNotContract is_c = DB.to.javaBean(SupplyResourceContractType.IsNotContract.class, r);
	    result.setIsNotContract(is_c);
	}

	result.getContractBase().add(NsiTable.toDom(r, "vc_nsi_58"));

	if (DB.ok(result.isVolumeDepends()) || DB.ok(result.isMeteringDeviceInformation())) {
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

	    boolean plannedVolumeInSubjects = DB.ok(r.get("isplannedvolume"))
		&& DB.to.String(r.get("plannedvolumetype")).equals(VocGisContractDimension.i.BY_CONTRACT.getName());

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

	    boolean plannedVolumeInObjects = DB.ok(r.get("isplannedvolume"))
		&& DB.to.String(r.get("plannedvolumetype")).equals(VocGisContractDimension.i.BY_HOUSE.getName());

	    boolean accrualInObjects = VocGisContractDimension.i.BY_HOUSE == VocGisContractDimension.i.forName(DB.to.String(r.get("accrualprocedure")));

	    for (Map<String, Object> o : objects) {
		o.put("apartmentnumber", o.get("premise.apartmentnumber"));
		o.put("roomnumber", o.get("premise.roomnumber"));

		ObjectAddress oa = DB.to.javaBean(ObjectAddress.class, o);

		oa.setTransportGUID(o.get("uuid").toString());

		if (VocGisContractDimension.i.BY_HOUSE == VocGisContractDimension.i.forName(DB.to.String(r.get("accrualprocedure")))) {
		    oa.setCountingResource(DB.to.String(r.get("countingresource")));
		}

		List<Map<String, Object>> services = (List<Map<String, Object>>) o.get("services");

		for (Map<String, Object> service : services) {

		    service.put("pairkey", service.get("pairkey.uuid"));

		    ObjectAddress.Pair pair = DB.to.javaBean(ObjectAddress.Pair.class, service);

		    ObjectAddress.Pair.HeatingSystemType hs = new ObjectAddress.Pair.HeatingSystemType ();

		    hs.setOpenOrNot(DB.ok(service.get("is_heat_open")) ? "Opened" : "Closed");
		    hs.setCentralizedOrNot(DB.ok(service.get("is_heat_centralized")) ? "Centralized" : "Decentralized");

		    pair.setHeatingSystemType(hs);

		    oa.getPair().add(pair);

		    if (plannedVolumeInObjects) {
			ObjectAddress.PlannedVolume v = DB.to.javaBean(ObjectAddress.PlannedVolume.class, service);
			oa.getPlannedVolume().add(v);
		    }

		    if (accrualInObjects) {
			oa.setMeteringDeviceInformation(DB.ok(r.get("mdinfo")));
		    }
		}

		result.getObjectAddress().add(oa);
	    }

	    if (accrualInObjects) {
		result.setCountingResource(null);
		result.setMeteringDeviceInformation(null);
	    }

	}

	toQuality(r, result);

	toOtherQuality(r, result);

	toTemperatureChart(r, result);

	return result;

    }

    private static void toQuality(Map<String, Object> r, SupplyResourceContractType result) {

	List<Map<String, Object>> quality = (List<Map<String, Object>>) r.get("quality");
	if (quality == null) {
	    throw new IllegalStateException("No supply resource contract quality fetched: " + r);
	}

	result.getQuality().clear();

	if (quality.isEmpty()) {
	    return;
	}
	// Показатели качества
	boolean qty_by_house = VocGisContractDimension.i.BY_HOUSE == VocGisContractDimension.i.forId(r.get(SupplyResourceContract.c.SPECQTYINDS.lc()));

	for (Map<String, Object> q : quality) {
	    q.put("okei", q.get("code_vc_okei"));
	    q.put("startrange", q.get("indicatorvalue_from"));
	    q.put("endrange", q.get("indicatorvalue_to"));
	    q.put("correspond", q.get("indicatorvalue_is"));
	    q.put("number", q.get("indicatorvalue"));
	    q.put("pairkey", qty_by_house ? q.get("pair.uuid") : q.get("subj.uuid"));
	    q.put("addressobjectkey", qty_by_house? q.get("addressobject.uuid") : null);

	    Quality quality_item = DB.to.javaBean(Quality.class, q);

	    quality_item.setQualityIndicator(NsiTable.toDom(q, "vc_nsi_276"));

	    Quality.IndicatorValue i_value = DB.to.javaBean(Quality.IndicatorValue.class, q);

	    VocGisContractQualityLevelType.i id_type = VocGisContractQualityLevelType.i.forId(q.get("vc_nsi_276.id_type"));

	    switch (id_type) {
		case RANGE:
		    i_value.setNumber(null);
		    i_value.setCorrespond(null);
		    break;
		case NUMBER:
		    i_value.setStartRange(null);
		    i_value.setEndRange(null);
		    i_value.setCorrespond(null);
		    break;
		case CORRESPOND:
		    i_value.setStartRange(null);
		    i_value.setEndRange(null);
		    i_value.setNumber(null);
		    i_value.setCorrespond(DB.ok(q.get("indicatorvalue_is")));
		break;
	    }

	    quality_item.setIndicatorValue(i_value);

	    result.getQuality().add(quality_item);
	}
    }

    private static void toOtherQuality(Map<String, Object> r, SupplyResourceContractType result) {

	List<Map<String, Object>> other_quality = (List<Map<String, Object>>) r.get("other_quality");
	if (other_quality == null) {
	    throw new IllegalStateException("No supply resource contract quality fetched: " + r);
	}

	result.getOtherQualityIndicator().clear();

	if (other_quality.isEmpty()) {
	    return;
	}
	// Показатели качества
	boolean qty_by_house = VocGisContractDimension.i.BY_HOUSE == VocGisContractDimension.i.forId(r.get(SupplyResourceContract.c.SPECQTYINDS.lc()));

	for (Map<String, Object> q : other_quality) {
	    q.put("okei", q.get("code_vc_okei"));
	    q.put("startrange", q.get("indicatorvalue_from"));
	    q.put("endrange", q.get("indicatorvalue_to"));
	    q.put("correspond", q.get("indicatorvalue_is"));
	    q.put("number", q.get("indicatorvalue"));
	    q.put("indicatorname", q.get("label"));
	    q.put("pairkey", qty_by_house ? q.get("pair.uuid") : q.get("subj.uuid"));
	    q.put("addressobjectkey", qty_by_house? q.get("addressobject.uuid") : null);

	    OtherQualityIndicator i_value = DB.to.javaBean(OtherQualityIndicator.class, q);

	    VocGisContractQualityLevelType.i id_type = VocGisContractQualityLevelType.i.forId(q.get("id_type"));

	    switch (id_type) {
		case RANGE:
		    i_value.setNumber(null);
		    i_value.setCorrespond(null);
		    break;
		case NUMBER:
		    i_value.setStartRange(null);
		    i_value.setEndRange(null);
		    i_value.setCorrespond(null);
		    break;
		case CORRESPOND:
		    i_value.setStartRange(null);
		    i_value.setEndRange(null);
		    i_value.setNumber(null);
		    i_value.setCorrespond(DB.ok(q.get("indicatorvalue_is")));
		break;
	    }

	    result.getOtherQualityIndicator().add(i_value);
	}
    }

    private static void toTemperatureChart(Map<String, Object> r, SupplyResourceContractType result) {

	List<Map<String, Object>> temperature_chart = (List<Map<String, Object>>) r.get("temperature_chart");
	if (temperature_chart == null) {
	    throw new IllegalStateException("No supply resource contract quality fetched: " + r);
	}

	result.getTemperatureChart().clear();

	if (temperature_chart.isEmpty()) {
	    return;
	}

	for (Map<String, Object> t : temperature_chart) {

	    t.put("addressobjectkey", t.get("addressobject.uuid"));

	    TemperatureChart temperature = DB.to.javaBean(TemperatureChart.class, t);

	    result.getTemperatureChart().add(temperature);
	}
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

	if (d == 99) {
	    d = -1;
	}

	start.setStartDate(d);
	start.setNextMonth(DB.ok(r.get(f + "_nxt"))? true : null);

	final SupplyResourceContractType.Period.End   end = new SupplyResourceContractType.Period.End();
	f = SupplyResourceContract.c.DDT_M_END.lc();
	if (r.get(f) == null) {
	    return null;
	}
	d = Byte.parseByte(DB.to.String(r.get(f)));

	if (d == 99) {
	    d = -1;
	}

	end.setEndDate(d);
	end.setNextMonth(DB.ok(r.get(f + "_nxt"))? true : null);

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
	NsiTable nsi54 = NsiTable.getNsiTable(54);
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
	    .toMaybeOne(nsi54, "AS vc_nsi_54", "code", "guid").on("ctr.code_vc_nsi_54=vc_nsi_54.code AND vc_nsi_54.isactual = 1")
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

	// Предметы договора
	r.put("subjects", db.getList(m
	    .select(SupplyResourceContractSubject.class, "AS root", "*")
	    .toOne(VocNsi3.class, "AS vc_nsi_3", "code", "guid").on("root.code_vc_nsi_3=vc_nsi_3.code AND vc_nsi_3.isactual = 1")
	    .toOne(VocNsi239.class, "AS vc_nsi_239", "code", "guid").on("root.code_vc_nsi_239=vc_nsi_239.code AND vc_nsi_239.isactual = 1")
	    .where(SupplyResourceContractSubject.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IS NULL")
	    .and("is_deleted", 0)
	));

	Map<UUID, Map<String, Object>> id2o = new HashMap<>();

	// ОЖФ
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

	// Поставляемые ресурсы ОЖФ
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

	// Показатели качества
	boolean qty_by_house = VocGisContractDimension.i.BY_HOUSE == VocGisContractDimension.i.forId(r.get(SupplyResourceContract.c.SPECQTYINDS.lc()));

	r.put("quality", db.getList(m
	    .select(SupplyResourceContractQualityLevel.class, "AS root", "*")
	    .toOne(VocNsi276.class, "AS vc_nsi_276", "code", "guid", "id_type")
		.on("root.code_vc_nsi_276=vc_nsi_276.code AND vc_nsi_276.isactual = 1")
	    .toOne(VocNsi239.class, "AS vc_nsi_239", "code").on("vc_nsi_276.guid_vc_nsi_239=vc_nsi_239.guid AND vc_nsi_239.isactual = 1")
	    .toOne(SupplyResourceContractSubject.class, "AS subj", "uuid").on()
	    .toMaybeOne(SupplyResourceContractObject.class,  "AS addressobject", "uuid").on("addressobject.uuid = subj.uuid_sr_ctr_obj")
	    .toMaybeOne(SupplyResourceContractSubject.class, "AS pair", "uuid")
	    .on("pair.uuid_sr_ctr = root.uuid_sr_ctr "
		+ " AND pair.is_deleted = 0 "
		+ " AND pair.uuid_sr_ctr_obj IS NULL "
		+ " AND subj.code_vc_nsi_239 = pair.code_vc_nsi_239 "
		+ " AND subj.code_vc_nsi_3   = pair.code_vc_nsi_3 "
		+ " AND (subj.endsupplydate <= pair.endsupplydate OR pair.endsupplydate IS NULL) "
		+ " AND (subj.startsupplydate >= pair.startsupplydate) "
	    )
	    .where(SupplyResourceContractQualityLevel.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and("is_deleted", 0)
	));

	// Иные показатели качества
	r.put("other_quality", db.getList(m
	    .select(SupplyResourceContractOtherQualityLevel.class, "AS root", "*")
	    .toOne(SupplyResourceContractSubject.class, "AS subj", "uuid").on()
	    .toMaybeOne(SupplyResourceContractObject.class, "AS addressobject", "uuid").on("addressobject.uuid = subj.uuid_sr_ctr_obj")
	    .toMaybeOne(SupplyResourceContractSubject.class, "AS pair", "uuid")
	    .on("pair.uuid_sr_ctr = root.uuid_sr_ctr "
		+ " AND pair.is_deleted = 0 "
		+ " AND pair.uuid_sr_ctr_obj IS NULL "
		+ " AND subj.code_vc_nsi_239 = pair.code_vc_nsi_239 "
		+ " AND subj.code_vc_nsi_3   = pair.code_vc_nsi_3 "
		+ " AND (subj.endsupplydate <= pair.endsupplydate OR pair.endsupplydate IS NULL) "
		+ " AND (subj.startsupplydate >= pair.startsupplydate) "
	    )
	    .where(SupplyResourceContractOtherQualityLevel.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and("is_deleted", 0)
	));

	// Температурный график
	r.put("temperature_chart", db.getList(m
	    .select(SupplyResourceContractTemperatureChart.class, "AS root", "*")
	    .toMaybeOne(SupplyResourceContractObject.class, "AS addressobject", "uuid").on()
	    .where(SupplyResourceContractTemperatureChart.c.UUID_SR_CTR, r.get("ctr.uuid"))
	    .and(SupplyResourceContractTemperatureChart.c.UUID_SR_CTR_OBJ.lc() + (qty_by_house ? " IS NOT NULL" : " IS NULL"))
	    .and("is_deleted", 0)
	));
    }
}