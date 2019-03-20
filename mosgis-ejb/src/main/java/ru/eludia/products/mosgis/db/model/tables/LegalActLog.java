package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi237;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi324;
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;
import ru.gosuslugi.dom.schema.integration.uk.ImportDocumentMunicipalRequest;
import ru.gosuslugi.dom.schema.integration.uk.ImportDocumentRegionRequest;

public class LegalActLog extends GisWsLogTable {

    public LegalActLog () {

        super ("tb_legal_acts__log", "История редактирования НПА", LegalAct.class
            , EnTable.c.class
	    , AttachTable.c.class
            , LegalAct.c.class
        );
    }

    public static ImportDocumentRegionRequest toImportDocumentsRegionRequest(Map<String, Object> r) {
	final ImportDocumentRegionRequest result = new ImportDocumentRegionRequest();

	ImportDocumentRegionRequest.Document d = new ImportDocumentRegionRequest.Document();
	d.setTransportGUID(UUID.randomUUID().toString());
	d.setImportDocument(toImportDocumentRegion(r));
	result.getDocument().add(d);

	return result;
    }

    public static ImportDocumentMunicipalRequest toImportDocumentsMunicipalRequest(Map<String, Object> r) {
	final ImportDocumentMunicipalRequest result = new ImportDocumentMunicipalRequest();

	ImportDocumentMunicipalRequest.Document d = new ImportDocumentMunicipalRequest.Document();
	d.setTransportGUID(UUID.randomUUID().toString());
	d.setImportDocument(toImportDocumentMunicipal(r));
	result.getDocument().add(d);

	return result;
    }

    private static ImportDocumentRegionRequest.Document.ImportDocument toImportDocumentRegion(Map<String, Object> r) {

	ImportDocumentRegionRequest.Document.ImportDocument result = DB.to.javaBean(ImportDocumentRegionRequest.Document.ImportDocument.class, r);

	result.setDocumentType(NsiTable.toDom(r, "vc_nsi_324"));

	if (DB.ok(r.get(LegalAct.c.SCOPE.lc()))) {

	    List<Map<String, Object>> oktmos = (List<Map<String, Object>>) r.get("oktmos");

	    if (oktmos == null || oktmos.isEmpty()) {
		throw new IllegalStateException("No regional legal act oktmos fetched: " + r);
	    }
	    for (Map<String, Object> o : oktmos) {
		result.getMunicipal().add(VocOktmo.createOKTMORef(DB.to.Long(o.get("code"))));
	    }
	    result.setRegion(null);
	} else {
	    result.setRegion(NsiTable.toDom(r, "vc_nsi_237"));
	}

	if (VocLegalActLevel.i.REGIONAL.getId() == DB.to.Long(r.get(LegalAct.c.LEVEL_.lc()))) {
	    // FIXME: fix ExportLegalActsMDB.sendFileThenSoap context
	    r.put("attachmentguid", UUID.fromString("00000000-0000-0000-0000-000000000000"));
	    r.put("attachmenthash", "0000000000000000");
	}

	result.setAttachment(LegalAct.toAttachmentType(r));

	return result;
    }

    private static ImportDocumentMunicipalRequest.Document.ImportDocument toImportDocumentMunicipal(Map<String, Object> r) {

	ImportDocumentMunicipalRequest.Document.ImportDocument result = DB.to.javaBean(ImportDocumentMunicipalRequest.Document.ImportDocument.class, r);

	result.setDocumentType(NsiTable.toDom(r, "vc_nsi_324"));

	List<Map<String, Object>> oktmos = (List<Map<String, Object>>) r.get("oktmos");
	Map<String, Object> o = oktmos.get(0);
	if (o == null || o.isEmpty()){
	    throw new IllegalStateException("No municipal legal act oktmos fetched: " + r);
	}
	result.setMunicipal(VocOktmo.createOKTMORef(DB.to.Long(o.get("code"))));

	result.setAttachment(LegalAct.toAttachmentType(r));

	return result;
    }

    public static ImportDocumentMunicipalRequest toDeleteDocumentsMunicipalRequest(Map<String, Object> r) {

	final ImportDocumentMunicipalRequest result = new ImportDocumentMunicipalRequest();

	ImportDocumentMunicipalRequest.Document d = new ImportDocumentMunicipalRequest.Document();

	d.setTransportGUID(UUID.randomUUID().toString());

	ImportDocumentMunicipalRequest.Document.AnnulmentDocument ad = DB.to.javaBean(ImportDocumentMunicipalRequest.Document.AnnulmentDocument.class, r);

	d.setAnnulmentDocument(ad);

	result.getDocument().add(d);

	return result;
    }

    public static ImportDocumentRegionRequest toDeleteDocumentsRegionRequest(Map<String, Object> r) {

	final ImportDocumentRegionRequest result = new ImportDocumentRegionRequest();

	ImportDocumentRegionRequest.Document d = new ImportDocumentRegionRequest.Document();

	d.setTransportGUID(UUID.randomUUID().toString());

	ImportDocumentRegionRequest.Document.AnnulmentDocument ad = DB.to.javaBean(ImportDocumentRegionRequest.Document.AnnulmentDocument.class, r);

	d.setAnnulmentDocument(ad);

	result.getDocument().add(d);

	return result;
    }

    public static Map<String, Object> getForExport(DB db, String id) throws SQLException {

	final Model m = db.getModel();

	final Map<String, Object> r = db.getMap(m
	    .get(LegalActLog.class, id, "*")
	    .toOne(LegalAct.class, "AS r",
		 EnTable.c.UUID.lc(),
		 LegalAct.c.ID_CTR_STATUS.lc()
	    ).on()
	    .toOne(VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on("r.uuid_org=org.uuid")
	    .toOne(VocNsi324.class, "code", "guid").on("r.code_vc_nsi_324=vc_nsi_324.code AND vc_nsi_324.isactual=1")
	    .toMaybeOne(VocNsi237.class, "code", "guid").on("r.code_vc_nsi_237=vc_nsi_237.code AND vc_nsi_237.isactual=1")
	);

	r.put("oktmos", db.getList(LegalActOktmo.select(db, r.get("r.uuid"))));

	return r;
    }
}