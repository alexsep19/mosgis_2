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
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi301;
import ru.gosuslugi.dom.schema.integration.nsi.ImportBaseDecisionMSPRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ImportBaseDecisionMSPType;

public class BaseDecisionMSPLog extends GisWsLogTable {

    public BaseDecisionMSPLog () {

        super (BaseDecisionMSP.TABLE_NAME + "__log", "История редактирования НСИ 302", BaseDecisionMSP.class
            , EnTable.c.class
            , BaseDecisionMSP.c.class
        );
    }

    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        return db.getMap (m                
            .get (BaseDecisionMSPLog.class, id, "*")
            .toOne (BaseDecisionMSP.class, "AS r"
                , EnTable.c.UUID.lc ()
            ).on ()
            .toOne (VocNsi301.class, "code", "guid").on ("r.code_vc_nsi_301=vc_nsi_301.code AND vc_nsi_301.isactual=1")
            .toMaybeOne (VocOrganization.class, "AS org", 
                "orgppaguid AS orgppaguid"
            ).on ("r.uuid_org=org.uuid")
        );
        
    }
        
    public static ImportBaseDecisionMSPRequest toImportBaseDecisionMSPRequest (Map<String, Object> r) {

        final ImportBaseDecisionMSPRequest result = new ImportBaseDecisionMSPRequest ();

	if (DB.ok(r.get("is_deleted"))) {
	    result.getDeleteBaseDecisionMSP().add(toDeleteBaseDecisionMSP(r));
	} else {
	    result.getImportBaseDecisionMSP().add(toImportBaseDecisionMSP(r));
	}
        
        return result;
        
    }

    private static ImportBaseDecisionMSPRequest.DeleteBaseDecisionMSP toDeleteBaseDecisionMSP (Map<String, Object> r) {
        final ImportBaseDecisionMSPRequest.DeleteBaseDecisionMSP result = DB.to.javaBean (ImportBaseDecisionMSPRequest.DeleteBaseDecisionMSP.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }

    private static ImportBaseDecisionMSPType toImportBaseDecisionMSP (Map<String, Object> r) {
        
        final ImportBaseDecisionMSPType result = DB.to.javaBean (ImportBaseDecisionMSPType.class, r);
        
        result.setDecisionType(NsiTable.toDom (r, "vc_nsi_301"));
        
        result.setTransportGUID (UUID.randomUUID ().toString ());
        
        return result;
        
    }
}