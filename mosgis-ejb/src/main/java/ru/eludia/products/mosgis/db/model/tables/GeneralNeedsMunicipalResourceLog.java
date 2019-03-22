package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi2;
import ru.gosuslugi.dom.schema.integration.nsi.ImportGeneralNeedsMunicipalResourceRequest;

public class GeneralNeedsMunicipalResourceLog extends GisWsLogTable {

    public GeneralNeedsMunicipalResourceLog () {

        super (GeneralNeedsMunicipalResource.TABLE_NAME + "__log", "История редактирования НСИ 337", GeneralNeedsMunicipalResource.class
            , EnTable.c.class
            , GeneralNeedsMunicipalResource.c.class
        );

    }
    
    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        return db.getMap (m                
            .get (GeneralNeedsMunicipalResourceLog.class, id, "*")
            .toOne (GeneralNeedsMunicipalResource.class, "AS r"
                , EnTable.c.UUID.lc ()
            ).on ()
            .toOne (VocNsi2.class, "code", "guid").on ("r.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1")
            .toMaybeOne (VocOrganization.class, "AS org", 
                "orgppaguid AS orgppaguid"
            ).on ("r.uuid_org=org.uuid")
        );
        
    }
        
    public static ImportGeneralNeedsMunicipalResourceRequest toImportGeneralNeedsMunicipalResourceRequest (Map<String, Object> r) {

        final ImportGeneralNeedsMunicipalResourceRequest result = new ImportGeneralNeedsMunicipalResourceRequest ();

        if (DB.ok (r.get ("is_deleted"))) {
            result.getDeleteGeneralMunicipalResource ().add (toDeleteGeneralMunicipalResource (r));
        }
        else {
            result.getTopLevelMunicipalResource ().add (toTopLevelMunicipalResource (r));
        }
        
        return result;
        
    }
    
    private static ImportGeneralNeedsMunicipalResourceRequest.DeleteGeneralMunicipalResource toDeleteGeneralMunicipalResource (Map<String, Object> r) {
        final ImportGeneralNeedsMunicipalResourceRequest.DeleteGeneralMunicipalResource result = DB.to.javaBean (ImportGeneralNeedsMunicipalResourceRequest.DeleteGeneralMunicipalResource.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }    
    
    private static ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource toTopLevelMunicipalResource (Map<String, Object> r) {
        final ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource result = DB.to.javaBean (ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.getImportGeneralMunicipalResource ().add (toImportGeneralMunicipalResource (r));
        return result;
    }    
    
    private static ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource.ImportGeneralMunicipalResource toImportGeneralMunicipalResource (Map<String, Object> r) {
        
        final ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource.ImportGeneralMunicipalResource result = DB.to.javaBean (ImportGeneralNeedsMunicipalResourceRequest.TopLevelMunicipalResource.ImportGeneralMunicipalResource.class, r);
        
        result.setMunicipalResourceRef (NsiTable.toDom (r, "vc_nsi_2"));
        
        if (result.isSortOrderNotDefined ()) {
            result.setSortOrder (null);
        } 
        else {
            result.setSortOrderNotDefined (null);
        }
        
        result.setTransportGUID (UUID.randomUUID ().toString ());
        
        return result;
        
    }

}