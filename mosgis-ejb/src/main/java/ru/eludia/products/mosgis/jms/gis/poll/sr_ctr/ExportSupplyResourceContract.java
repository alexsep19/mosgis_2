package ru.eludia.products.mosgis.jms.gis.poll.sr_ctr;

import java.util.Map;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.c;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractResultType;

public class ExportSupplyResourceContract {

    public static Map<String, Object> toHASH(ExportSupplyResourceContractResultType t) {

	Map<String, Object> r = DB.HASH(
	    c.ID_CTR_STATUS.lc(), VocGisStatus.i.APPROVED.getId(),
	    c.ID_CTR_STATUS_GIS.lc(), VocGisStatus.i.APPROVED.getId(),
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

	return r;
    }
}
