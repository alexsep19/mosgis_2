package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi79;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportDocumentType.LoadDocument;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramRequest.ImportRegionalProgram.RegionalProgramDocument;
import ru.gosuslugi.dom.schema.integration.capital_repair.RegionalProgramPasportType;

public class OverhaulRegionalProgramLog extends GisWsLogTable {

    public OverhaulRegionalProgramLog () {
        
        super ("tb_oh_reg_programs__log", "Региональные программы капитального ремонта: история изменений",
                OverhaulRegionalProgram.class,
                EnTable.c.class,
                OverhaulRegionalProgram.c.class);
        
    }
    
    public static ImportRegionalProgramRequest toAnnulRegionalProgramRequest (Map <String, Object> r) {
        final ImportRegionalProgramRequest result = new ImportRegionalProgramRequest ();
        result.setImportRegionalProgram (toImportRegionalProgramAnnul (r));
        return result;
    }
    
    public static ImportRegionalProgramRequest toDeleteRegionalProgramRequest (Map <String, Object> r) {
        final ImportRegionalProgramRequest result = new ImportRegionalProgramRequest ();
        result.setImportRegionalProgram (toImportRegionalProgramDelete (r));
        return result;
    }
    
    public static ImportRegionalProgramRequest toImportRegionalProgramRequest (Map <String, Object> r, boolean isProject) {
        final ImportRegionalProgramRequest result = new ImportRegionalProgramRequest ();
        if (isProject)
            result.setImportRegionalProgram (toImportRegionalProgramProject (r));
        else
            result.setImportRegionalProgram (toImportRegionalProgramPublish (r));
        return result;
    }
    
    private static ImportRegionalProgramRequest.ImportRegionalProgram toImportRegionalProgramProject (Map <String, Object> r) {
        final ImportRegionalProgramRequest.ImportRegionalProgram result = new ImportRegionalProgramRequest.ImportRegionalProgram ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setLoadRegionalProgram(toRegionalProgramPasportType (r));
        
        for (Map <String, Object> document: (List <Map <String, Object>>) r.get ("documents")) {
            RegionalProgramDocument doc = new RegionalProgramDocument ();
            doc.setTransportGuid (UUID.randomUUID ().toString ());
            doc.setLoadDocument (toLoadDocument (document));
            result.getRegionalProgramDocument().add (doc);
        }
        
        return result;
    }
    
    private static ImportRegionalProgramRequest.ImportRegionalProgram toImportRegionalProgramPublish (Map <String, Object> r) {
        final ImportRegionalProgramRequest.ImportRegionalProgram result = new ImportRegionalProgramRequest.ImportRegionalProgram ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setPublishRegionalProgram(Boolean.TRUE);
        result.setRegionalProgramGuid (r.get ("regionalprogramguid").toString ());
        return result;
    }
    
    private static ImportRegionalProgramRequest.ImportRegionalProgram toImportRegionalProgramDelete(Map <String, Object> r) {
        final ImportRegionalProgramRequest.ImportRegionalProgram result = new ImportRegionalProgramRequest.ImportRegionalProgram ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setDeleteRegionalProgram(Boolean.TRUE);
        result.setRegionalProgramGuid (r.get ("regionalprogramguid").toString ());
        return result;
    }
    
    private static ImportRegionalProgramRequest.ImportRegionalProgram toImportRegionalProgramAnnul(Map<String, Object> r) {
        final ImportRegionalProgramRequest.ImportRegionalProgram result = new ImportRegionalProgramRequest.ImportRegionalProgram ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setCancelRegionalProgram(Boolean.TRUE);
        result.setRegionalProgramGuid (r.get ("regionalprogramguid").toString ());
        return result;
    }
    
    private static LoadDocument toLoadDocument (Map<String, Object> document) {
        final LoadDocument result = DB.to.javaBean (LoadDocument.class, document);
        result.setKind (NsiTable.toDom (document.get ("nsi_79.id").toString (), UUID.fromString (document.get ("nsi_79.guid").toString ())));
        for (Map <String, Object> file: (List <Map <String, Object>>) document.get ("files")) {
            result.getAttachDocument ().add (OverhaulRegionalProgramFile.toAttachmentType (file));
        }
        return result;
    }
    
    private static RegionalProgramPasportType toRegionalProgramPasportType (Map<String, Object> r) {
        final RegionalProgramPasportType result = DB.to.javaBean (RegionalProgramPasportType.class, r);
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        Map <String, Object> record = db.getMap (db.getModel ()
                .get   (OverhaulRegionalProgramLog.class, id, "*")
                .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ()
        );
        
        record.put ("documents", db.getList (db.getModel ()
                .select  (OverhaulRegionalProgramDocument.class, "AS doc", "*")
                .toOne   (Nsi79.class, "AS nsi_79", "*").on ("doc.code_nsi_79 = nsi_79.id")
                .where   ("program_uuid", record.get ("uuid_object"))
                .and     ("is_deleted", 0)
                .orderBy (OverhaulRegionalProgramDocument.c.NUMBER_.lc ())
        ));
        
        for (Map <String, Object> document: (List <Map <String, Object>>) record.get ("documents")) {
            
            document.put ("date", document.get ("date_"));
            document.put ("number", document.get ("number_"));
            
            document.put ("files", db.getList (db.getModel ()
                    .select  (OverhaulRegionalProgramFile.class, "*")
                    .where   ("uuid_oh_reg_pr_doc", document.get ("uuid"))
                    .and     ("id_status", 1)
                    .toOne   (OverhaulRegionalProgramFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            ));
            
        }
        
        return record;
        
    }
    
}