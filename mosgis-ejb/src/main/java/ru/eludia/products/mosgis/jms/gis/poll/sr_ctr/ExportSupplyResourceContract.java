package ru.eludia.products.mosgis.jms.gis.poll.sr_ctr;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.c;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractType.ContractSubject;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractType.PlannedVolume;

public class ExportSupplyResourceContract {

    public static Map<String, Object> toHASH(ExportSupplyResourceContractResultType t) {

	final VocGisStatus.i status = toStatusGis (t.getVersionStatus());

	final Map<String, Object> r = DB.HASH(
	    c.ID_CTR_STATUS.lc(), status.getId(),
	    c.ID_CTR_STATUS_GIS.lc(), status.getId(),
	    c.CONTRACTGUID.lc(), t.getContractGUID(),
	    c.CONTRACTROOTGUID.lc(), t.getContractRootGUID()
	);

	if (!DB.ok(r.get(c.CONTRACTROOTGUID.lc()))) {
	    Logger.getLogger(ExportSupplyResourceContract.class.getName()).warning("no ctr root guid " + t.getContractGUID());
	    return null;
	}

	try {
	    CustomerSetter.setCustomer (r, t);
	} catch (Exception ex) {
	    String msg = t.getContractRootGUID().toString() + ": " + ex.getMessage();
	    Logger.getLogger(ExportSupplyResourceContract.class.getName()).warning(msg);
	    return null;
	}

	if (DB.ok(t.getTerminateContract())) {
	    r.put (c.ID_CTR_STATUS.lc(), VocGisStatus.i.TERMINATED.getId());
	    r.put (c.ID_CTR_STATUS_GIS.lc(), VocGisStatus.i.TERMINATED.getId());
	    r.put (c.CODE_VC_NSI_54.lc(), t.getTerminateContract().getReasonRef().getCode());
	    r.put (c.TERMINATE.lc(), t.getTerminateContract().getTerminate());
	}

	if (DB.ok(t.getAnnulmentContract())) {
	    r.put (c.ID_CTR_STATUS.lc(), VocGisStatus.i.ANNUL.getId());
	    r.put (c.ID_CTR_STATUS_GIS.lc(), VocGisStatus.i.ANNUL.getId());
	    r.put (c.REASONOFANNULMENT.lc(), t.getAnnulmentContract().getReasonOfAnnulment());
	}

	Map<String, Object> rr = DB.HASH();
        ExportSupplyResourceContractResultType.IsNotContract isNotContract = t.getIsNotContract ();
        if (isNotContract != null) {
            rr = DB.to.Map (isNotContract);
            rr.put (c.IS_CONTRACT.lc (), 0);
        } else {
	    ExportSupplyResourceContractResultType.IsContract isContract = t.getIsContract ();
	    if (isContract != null) {
		rr = DB.to.Map (isContract);
		rr.put (c.IS_CONTRACT.lc (), 1);
	    }
	}
	        
        rr.entrySet ().forEach ((kv) -> {
            r.put (kv.getKey (), kv.getValue ());
        });

	r.put(c.AUTOROLLOVER.lc(), DB.ok(t.isAutomaticRollOverOneYear())? 1 : 0);
	r.put(c.COMPLETIONDATE.lc(), t.getComptetionDate());

	r.put (c.VOLUMEDEPENDS.lc(), DB.ok(t.isVolumeDepends()));

	if (t.getContractBase().size() > 0) {
	    r.put (c.CODE_VC_NSI_58.lc(), t.getContractBase().get(0).getCode());
	}

	r.put(c.ISPLANNEDVOLUME.lc(), DB.ok(t.isIsPlannedVolume())? 1 : 0);
	r.put(c.PLANNEDVOLUMETYPE.lc(), VocGisContractDimension.i.forName(DB.to.String(t.getPlannedVolumeType())));

	r.put(c.COUNTINGRESOURCE.lc(), DB.eq(t.getCountingResource(), "R"));
	r.put(c.MDINFO.lc(), DB.ok(t.isMeteringDeviceInformation())? 1 : 0);
	r.put(c.SPECQTYINDS.lc(), VocGisContractDimension.i.forName(DB.to.String(t.getSpecifyingQualityIndicators())));
	r.put(c.ONETIMEPAYMENT.lc(), DB.ok(t.isOneTimePayment()) ? 1 : 0);

	if (DB.ok (t.getBillingDate())) {
	    r.put(c.DDT_D_START.lc(), toSrCtrDate(t.getBillingDate().getDate()));
	    r.put(c.DDT_D_START_NXT.lc(), DB.eq(t.getBillingDate().getDateType(), "N") ? 1 : 0);
	}

	if (DB.ok (t.getPaymentDate())) {
	    r.put(c.DDT_I_START.lc(), toSrCtrDate(t.getPaymentDate().getDate()));
	    r.put(c.DDT_I_START_NXT.lc(), DB.eq(t.getPaymentDate().getDateType(), "N") ? 1 : 0);
	}

	if (DB.ok (t.getProvidingInformationDate())) {
	    r.put(c.DDT_N_START.lc(), toSrCtrDate(t.getProvidingInformationDate().getDate()));
	    r.put(c.DDT_N_START_NXT.lc(), DB.eq(t.getProvidingInformationDate().getDateType(), "N") ? 1 : 0);
	}

	if (DB.ok (t.getPeriod())) {

	    r.put(c.DDT_M_START.lc(), t.getPeriod().getStart().getStartDate());
	    r.put(c.DDT_M_START_NXT.lc(), DB.ok(t.getPeriod().getStart().isNextMonth()) ? 1 : 0);

	    r.put(c.DDT_M_END.lc(), toSrCtrDate(t.getPeriod().getEnd().getEndDate()));
	    r.put(c.DDT_M_END_NXT.lc(), DB.ok(t.getPeriod().getEnd().isNextMonth()) ? 1 : 0);
	}

	addContractSubjects (r, t.getContractSubject (), t.getPlannedVolume());

	return r;
    }

    private static VocGisStatus.i toStatusGis (String versionStatus) {

	switch (versionStatus) {
	    case "Draft":
		return VocGisStatus.i.PROJECT;
	    case "Posted":
		return VocGisStatus.i.APPROVED;
	    case "Terminated":
		return VocGisStatus.i.TERMINATED;
	    case "Annul":
		return VocGisStatus.i.ANNUL;
	    default:
		Logger.getLogger(ExportSupplyResourceContract.class.getName()).log(Level.WARNING, "Unknown status " + versionStatus + ". Will use FAILED_STATE instead");
		return VocGisStatus.i.FAILED_STATE;
	}
    }

    private static Object toSrCtrDate(BigInteger dt) {

	if (dt == null) {
	   return dt;
	}

	return toSrCtrDate(dt.byteValue());
    }

    private static Object toSrCtrDate(byte dt) {
	return DB.eq(dt, "-1")? 99 : dt;
    }

    private static void addContractSubjects(Map<String, Object> r, List<ContractSubject> contractSubject, List<PlannedVolume> subjectVolumes) {

	r.put(SupplyResourceContractSubject.TABLE_NAME, contractSubject.stream().map(t -> toMap(t)).collect(Collectors.toList()));

	final Map<String, Map<String, Object>> transportguid2subj = new HashMap();

	for (Map<String, Object> i : (List <Map<String, Object>>) r.get(SupplyResourceContractSubject.TABLE_NAME)) {
	    transportguid2subj.put(DB.to.String(i.get("transportguid")), i);
	}

	for (PlannedVolume vol : subjectVolumes) {
	    final Map<String, Object> v = DB.to.Map(vol);
	    final Map<String, Object> subj = transportguid2subj.get(vol.getPairKey());
	    if (DB.ok (subj)) {
		subj.put(SupplyResourceContractSubject.c.VOLUME.lc(), v.get("volume"));
		subj.put(SupplyResourceContractSubject.c.UNIT.lc(), v.get("unit"));
		subj.put(SupplyResourceContractSubject.c.FEEDINGMODE.lc(), v.get("feedingmode"));
	    }

	}

    }

    private static Map<String, Object> toMap(ExportSupplyResourceContractType.ContractSubject cs) {
        Map<String, Object> result = DB.to.Map (cs);
        result.put (SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc (), cs.getServiceType ().getCode ());
        result.put (SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc (), cs.getMunicipalResource ().getCode ());
	result.put (EnTable.c.UUID.lc(), cs.getTransportGUID());
        return result;
    }
}
