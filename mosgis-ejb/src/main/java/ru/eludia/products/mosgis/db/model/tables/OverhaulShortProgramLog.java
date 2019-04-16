package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi79;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportDocumentType.LoadDocument;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanRequest.ImportPlan;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanRequest.ImportPlan.PlanDocument;
import ru.gosuslugi.dom.schema.integration.capital_repair.PlanPassportType;


public class OverhaulShortProgramLog extends GisWsLogTable {
    
    public OverhaulShortProgramLog () {
        
        super ("tb_oh_shrt_programs__log", "Краткосрочные программы капитального ремонта: история изменений",
                OverhaulShortProgram.class,
                EnTable.c.class,
                OverhaulShortProgram.c.class
        );
        
    }
    
    public static ImportPlanRequest toDeletePlanRequest (Map <String, Object> r) {
        ImportPlanRequest result = new ImportPlanRequest ();
        result.setImportPlan (toImportPlanDelete (r));
        return result;
    }
    
    private static ImportPlan toImportPlanDelete (Map <String, Object> r) {
        ImportPlan result = new ImportPlan ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setDeletePlan (Boolean.TRUE);
        result.setPlanGuid (r.get ("planguid").toString ());
        return result;
    }
    
    public static ImportPlanRequest toAnnulPlanRequest (Map <String, Object> r) {
        ImportPlanRequest result = new ImportPlanRequest ();
        result.setImportPlan (toImportPlanAnnul (r));
        return result;
    }
    
    private static ImportPlan toImportPlanAnnul (Map <String, Object> r) {
        ImportPlan result = new ImportPlan ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setCancelPlan (Boolean.TRUE);
        result.setPlanGuid (r.get ("planguid").toString ());
        return result;
    }
    
    public static ImportPlanRequest toImportPlanRequest (Map <String, Object> r, boolean isProject) {
        ImportPlanRequest result = new ImportPlanRequest ();
        if (isProject)
            result.setImportPlan (toImportPlanProject (r));
        else
            result.setImportPlan (toImportPlanPublish (r));
        return result;
    }
    
    private static ImportPlan toImportPlanProject (Map <String, Object> r) {
        ImportPlan result = new ImportPlan ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setLoadPlan (toPlanPassportType (r));
        
        for (Map <String, Object> document: (List <Map <String, Object>>) r.get ("documents")) {
            PlanDocument doc = new PlanDocument ();
            doc.setTransportGuid (document.get ("uuid").toString ());
            doc.setLoadDocument (toLoadDocument (document));
            result.getPlanDocument ().add (doc);
        }
        
        return result;
    }
    
    private static ImportPlan toImportPlanPublish (Map <String, Object> r) {
        ImportPlan result = new ImportPlan ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setPublishPlan (Boolean.TRUE);
        result.setPlanGuid (r.get ("planguid").toString ());
        return result;
    }
    
    private static LoadDocument toLoadDocument (Map <String, Object> document) {
        final LoadDocument result = DB.to.javaBean (LoadDocument.class, document);
        result.setKind (NsiTable.toDom (document.get ("nsi_79.id").toString (), UUID.fromString (document.get ("nsi_79.guid").toString ())));
        for (Map <String, Object> file: (List <Map <String, Object>>) document.get ("files")) {
            result.getAttachDocument ().add (OverhaulShortProgramFile.toAttachmentType (file));
        }
        return result;
    }
    
    private static PlanPassportType toPlanPassportType (Map <String, Object> r) {
        PlanPassportType result = DB.to.javaBean (PlanPassportType.class, r);
        result.setName (r.get ("programname").toString ());
        result.setTerritory (VocOktmo.createOKTMORef (45000000000L));
        result.setType ("Plan");
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        Map <String, Object> record = db.getMap (db.getModel ()
                .get   (OverhaulShortProgramLog.class, id, "*")
                .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ()
        );
        
        record.put ("documents", db.getList (db.getModel ()
                .select  (OverhaulShortProgramDocument.class, "AS doc", "*")
                .toOne   (Nsi79.class, "AS nsi_79", "*").on ("doc.code_nsi_79 = nsi_79.id")
                .where   ("program_uuid", record.get ("uuid_object"))
                .and     ("is_deleted", 0)
                .orderBy (OverhaulShortProgramDocument.c.NUMBER_.lc ())
        ));
        
        for (Map <String, Object> document: (List <Map <String, Object>>) record.get ("documents")) {
            
            document.put ("date", document.get ("date_"));
            document.put ("number", document.get ("number_"));
            
            document.put ("files", db.getList (db.getModel ()
                    .select  (OverhaulShortProgramFile.class, "*")
                    .where   ("uuid_oh_shrt_pr_doc", document.get ("uuid"))
                    .and     ("id_status", 1)
                    .toOne   (OverhaulShortProgramFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            ));
            
        }
        
        return record;
        
    }
    
}
