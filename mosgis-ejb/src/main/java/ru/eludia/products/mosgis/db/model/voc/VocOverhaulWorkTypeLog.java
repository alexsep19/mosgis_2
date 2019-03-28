package ru.eludia.products.mosgis.db.model.voc;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkType;

public class VocOverhaulWorkTypeLog extends GisWsLogTable {
    
    public VocOverhaulWorkTypeLog () {
        
        super ("vc_oh_wk_types__log", "Виды работ капитального ремонта: история изменеий", 
                VocOverhaulWorkType.class, 
                EnTable.c.class, 
                VocOverhaulWorkType.c.class);
        
    }
    
    public static ImportCapitalRepairWorkRequest toImportCapitalRepairWorRequest (Map <String, Object> r) {
        final ImportCapitalRepairWorkRequest result = new ImportCapitalRepairWorkRequest ();
        result.getImportCapitalRepairWork ().add (toImportCapitalRepairWork (r));
        return result;
    }
    
    private static ImportCapitalRepairWorkType toImportCapitalRepairWork(Map<String, Object> r) {
        final ImportCapitalRepairWorkType result = DB.to.javaBean (ImportCapitalRepairWorkType.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        if (r.containsKey("guid") && r.get ("guid") != null)
            result.setElementGuid (r.get ("guid").toString ());
        result.setServiceName (r.get ("servicename").toString ());
        result.setWorkGroupRef (NsiTable.toDom(r, "vc_nsi_218"));
        return result;
    }
    
    public Get getForExport (String id) {
        
        final NsiTable nsi_218 = NsiTable.getNsiTable (218);
        
        return (Get) getModel ()
            .get        (this, id, "*")
            .toOne      (VocOrganization.class, "AS org", "orgppaguid").on ()
            .toOne      (nsi_218, nsi_218.getLabelField ().getfName () + " AS vc_nsi_218", "code", "guid").on ("(code_vc_nsi_218 = vc_nsi_218.code AND vc_nsi_218.isactual = 1)");            

    }
    
}
