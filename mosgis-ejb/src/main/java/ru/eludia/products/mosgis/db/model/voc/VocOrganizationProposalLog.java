package ru.eludia.products.mosgis.db.model.voc;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.organizations_registry.ImportSubsidiaryRequest;

public class VocOrganizationProposalLog extends GisWsLogTable {

    public VocOrganizationProposalLog () {

        super ("vc_org_proposals__log", "История редактирования: создание обособленных подразделений и ФПИЮЛ"
            , VocOrganizationProposal.class
            , EnTable.c.class
            , VocOrganizationProposal.c.class
        );

    }
    
    public static ImportSubsidiaryRequest toImportSubsidiaryRequest (Map<String, Object> r) {
        final ImportSubsidiaryRequest result = new ImportSubsidiaryRequest ();
        result.getSubsidiary ().add (toImportSubsidiaryRequestSubsidiary (r));
        return result;
    }
    
    private static ImportSubsidiaryRequest.Subsidiary toImportSubsidiaryRequestSubsidiary (Map<String, Object> r) {
        final ImportSubsidiaryRequest.Subsidiary result = DB.to.javaBean (ImportSubsidiaryRequest.Subsidiary.class, r);
        return result;
    }
    
    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
            .toOne (VocOrganizationProposal.class, "AS r"
                , VocOrganizationProposal.c.ID_ORG_PR_STATUS.lc ()
            ).on ()
            .toOne (VocOrganization.class, "AS o"
                , VocOrganization.c.ORGVERSIONGUID.lc ()
            ).on ()
            ;
                
    }            
    
}