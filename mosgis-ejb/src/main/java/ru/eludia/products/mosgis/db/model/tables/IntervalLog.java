package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi3;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi239;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.volume_quality.BaseShortType;
import ru.gosuslugi.dom.schema.integration.volume_quality.BaseKindShortType;
import ru.gosuslugi.dom.schema.integration.volume_quality.ImportIntervalRequest;
import ru.gosuslugi.dom.schema.integration.volume_quality.IntervalType;

public class IntervalLog extends GisWsLogTable {

    public IntervalLog () {

        super ("tb_intervals__log", "История редактирования информации о перерывах РСО", Interval.class
            , EnTable.c.class
            , Interval.c.class
        );
    }

    public Get getForExport(String id) {

	return (Get) getModel()
	    .get(this, id, "*")
	    .toOne(Interval.class, "AS r",
		 Interval.c.ID_CTR_STATUS.lc()
	    ).on()
	    .toMaybeOne(SupplyResourceContract.class, "AS sr_ctr", "contractrootguid").on()
	    .toMaybeOne(VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on("r.uuid_org=org.uuid")
	    .toOne(VocNsi3.class, "AS vc_nsi_3", "code", "guid").on("r.code_vc_nsi_3=vc_nsi_3.code AND vc_nsi_3.isactual = 1")
	    .toOne(VocNsi239.class, "AS vc_nsi_239", "code", "guid").on("r.code_vc_nsi_239=vc_nsi_239.code AND vc_nsi_239.isactual = 1")
	;
    }

    public static void addItemsForExport(DB db, Map<String, Object> r) throws SQLException {

	r.put("objects", db.getList(db.getModel()
	    .select(IntervalObject.class, "*")
	    .toMaybeOne(Premise.class, "AS prem",
		 "livingroomguid AS livingroomguid",
		 "premisesguid AS premisesguid"
	    ).on()
	    .where(IntervalObject.c.UUID_INTERVAL, r.get("uuid_object"))
	    .and("is_deleted", 0)
	));
    }

    public static ImportIntervalRequest toImportIntervalRequest(Map<String, Object> r) {

	final ImportIntervalRequest result = DB.to.javaBean(ImportIntervalRequest.class, r);

	result.getImportInterval().add(toImportInterval(r));

	return result;
    }

    public static ImportIntervalRequest.ImportInterval toImportInterval(Map<String, Object> r) {

	final ImportIntervalRequest.ImportInterval result = DB.to.javaBean(ImportIntervalRequest.ImportInterval.class, r);

	result.setTransportGuid(UUID.randomUUID().toString());

	result.setBase(toBase(r));

	IntervalType interval = DB.to.javaBean(IntervalType.class, r);

	for (Map<String, Object> i : (List<Map<String, Object>>) r.get("objects")) {
	    interval.getAddressObject().add(IntervalObject.toAddressObject(i));
	}

	interval.setMunicipalService(NsiTable.toDom(r, "vc_nsi_3"));

	interval.setRatedResource(NsiTable.toDom(r, "vc_nsi_239"));

	result.setLoadInterval(interval);

	return result;
    }

    private static BaseShortType toBase(Map<String, Object> r) {

	BaseShortType base = new BaseShortType();

	base.setBaseKind(BaseKindShortType.DRSO);

	base.setContractRootGUID(DB.to.String(r.get("sr_ctr.contractrootguid")));

	return base;
    }
}