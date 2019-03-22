package ru.eludia.products.mosgis.db.model.incoming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalActOktmo;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi237;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;
import ru.gosuslugi.dom.schema.integration.uk.DocumentFederalExportType;
import ru.gosuslugi.dom.schema.integration.uk.DocumentMunicipalExportType;
import ru.gosuslugi.dom.schema.integration.uk.DocumentRegionExportType;
import ru.gosuslugi.dom.schema.integration.uk.ExportDocumentRequest;
import ru.gosuslugi.dom.schema.integration.uk.ExportDocumentType;

public class InLegalAct extends Table {

    public enum c implements ColEnum {
        
	UUID            (Type.UUID,      NEW_UUID, "Ключ"),
	TS              (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
	UUID_ORG        (VocOrganization.class, "Организация-инициатор импорта"),
	UUID_OUT_SOAP   (OutSoap.class, null, "Импорт"),

	LEVEL_          (VocLegalActLevel.class, null, "Уровень (сфера действия)"),
	ACCEPTSTARTDATE (Type.DATE, "Дата начала периода принятия органом государственной власти"),
	ACCEPTENDDATE   (Type.DATE, null, "Дата окончания периода принятия органом государственной власти"),
        ;
        
        @Override public Col getCol() {return col;}private Col col; private c(Type type, Object... p) {col = new Col(this, type, p);} private c(Class c, Object... p) {col = new Ref(this, c, p);}

    }

    public InLegalAct () {
        super ("in_" + LegalAct.TABLE_NAME, "Запросы на импорт нормативно-правовых актов");
        cols  (InLegalAct.c.class);
        pk    (c.UUID);
    }

    public static Map<String, Object> toHASH(ExportDocumentType doc) {

	final Map<String, Object> result = DB.HASH(
	    LegalAct.c.DOCUMENTGUID.lc(), UUID.fromString(doc.getDocumentGuid()),
	    LegalAct.c.UUID_ORG.lc(), null,
	    LegalAct.c.ID_CTR_STATUS.lc(), VocGisStatus.i.APPROVED,
	    LegalAct.c.ID_CTR_STATUS_GIS.lc(), VocGisStatus.i.APPROVED
	);

	DocumentRegionExportType dr = doc.getDocumentRegion();
	DocumentFederalExportType df = doc.getDocumentFederal();
	DocumentMunicipalExportType dm = doc.getDocumentMunicipal();

	List<Map<String, Object>> oktmos = new ArrayList<>();

	if (dr != null) {
	    result.put(LegalAct.c.LEVEL_.lc(), VocLegalActLevel.i.REGIONAL);
	    result.put(LegalAct.c.NAME.lc(), dr.getName());
	    result.put(LegalAct.c.DOCNUMBER.lc(), dr.getDocNumber());
	    result.put(LegalAct.c.APPROVEDATE.lc(), dr.getApproveDate());

	    result.put(LegalAct.c.CODE_VC_NSI_324.lc(), toDocumentType(dr.getDocumentType()));
//	    result.put(LegalAct.c.CODE_VC_NSI_237.lc(), dr.getRegion());
	    oktmos = LegalActOktmo.toHashList(dr.getMunicipal());

	    toAttachment(result, dr.getAttachment());
	}

	if (dm != null) {
	    result.put(LegalAct.c.LEVEL_.lc(), VocLegalActLevel.i.MUNICIPAL);
	    result.put(LegalAct.c.NAME.lc(), dm.getName());
	    result.put(LegalAct.c.DOCNUMBER.lc(), dm.getDocNumber());
	    result.put(LegalAct.c.APPROVEDATE.lc(), dm.getApproveDate());

	    result.put(LegalAct.c.CODE_VC_NSI_324.lc(), toDocumentType(dm.getDocumentType()));
	    oktmos = LegalActOktmo.toHashList(dm.getMunicipal());

	    toAttachment(result, dm.getAttachment());
	}

	if (df != null) {
	    return null;
	}

	result.put("oktmos", oktmos);

	result.put(LegalAct.c.SCOPE.lc(), oktmos.isEmpty()? 0 : 1);

	return result;
    }

    private static void toAttachment(Map<String, Object> r, AttachmentType attachment) {
	r.put(AttachTable.c.ID_STATUS.lc(), VocFileStatus.i.LOADING);
	r.put(AttachTable.c.ATTACHMENTHASH.lc(), attachment.getAttachmentHASH());
	r.put(AttachTable.c.ATTACHMENTGUID.lc(), attachment.getAttachment().getAttachmentGUID());
	r.put(AttachTable.c.LEN.lc(), 0);
	r.put(AttachTable.c.MIME.lc(), "application/pdf");
	r.put(AttachTable.c.LABEL.lc(), attachment.getName());
	r.put(AttachTable.c.DESCRIPTION.lc(), attachment.getDescription());
    }

    private static Object toDocumentType(NsiRef documentType) {
	if (documentType == null) {
	    return null;
	}
	return DB.to.String(documentType.getCode());
    }

    public static ExportDocumentRequest toExportDocumentsRequest(Map<String, Object> r) {

	ExportDocumentRequest result = DB.to.javaBean(ExportDocumentRequest.class, r);

	VocLegalActLevel.i level = VocLegalActLevel.i.forId(r.get(c.LEVEL_.lc()));

	switch (level) {
	    case REGIONAL:
		result.setRegion(NsiTable.toDom(r, "vc_nsi_237"));
		break;
	    case MUNICIPAL:
		result.setMunicipal(VocOktmo.createOKTMORef(DB.to.Long(r.get("vc_oktmo.code"))));
		break;
	    case FEDERAL:
		result.setFederal(true);
		break;
	}

	return result;
    }

    public static Get getForImport(UUID uuid) {

	return (Get) ModelHolder.getModel().get(InLegalAct.class, uuid, "*")
	    .toOne(VocOrganization.class, "orgppaguid AS ppa").on()
	    .toOne(VocNsi237.class, "guid", "code").on("vc_nsi_237.code = '77'")
	    .toOne(VocOktmo.class, "code").on("vc_oktmo.code = '45000000' AND vc_oktmo.section_code = '1'")
	;
    }
}