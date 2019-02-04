package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.infrastructure.ImportOKIRequest;

public class InfrastructureLog extends GisWsLogTable {

    


    public InfrastructureLog () {
        
        super ("tb_infrastructures__log", "История редактирования объектов коммунальной инфраструктуры", Infrastructure.class, EnTable.c.class, Infrastructure.c.class);
        
    }
    
    public static ImportOKIRequest toImportOKIRequest (Map<String, Object> r) {
        final ImportOKIRequest result = new ImportOKIRequest ();
        result.getRKIItem ().add (toRKIItem (r));
        return result;            
    }
    
    private static ImportOKIRequest.RKIItem toRKIItem (Map<String, Object> r) {
        final ImportOKIRequest.RKIItem result = DB.to.javaBean (ImportOKIRequest.RKIItem.class, r);
        result.setTransportGUID(UUID.randomUUID ().toString ());
        result.setOKI(toOKI (r));
        return result;
    }
    
    private static ImportOKIRequest.RKIItem.OKI toOKI (Map<String, Object> r) {
        final ImportOKIRequest.RKIItem.OKI result = DB.to.javaBean (ImportOKIRequest.RKIItem.OKI.class, r);
        result.setBase (NsiTable.toDom (r, "vc_nsi_39"));
        return result;
    }
    
    public Get getForExport (String id) {
        
        final NsiTable nsi_39 = NsiTable.getNsiTable (39);
        
        return (Get) getModel ()
            .get   (this, id, "*")
            .toOne (Infrastructure.class, "AS r", Infrastructure.c.ID_IS_STATUS.lc ()).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
            .toOne (nsi_39, nsi_39.getLabelField ().getfName () + " AS vc_nsi_39", "code", "guid").on ("(r.code_vc_nsi_39 = vc_nsi_39.code AND vc_nsi_39.isactual=1)");
    }
    
}
