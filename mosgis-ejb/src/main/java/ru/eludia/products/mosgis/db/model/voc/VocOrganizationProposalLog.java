package ru.eludia.products.mosgis.db.model.voc;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.organizations_registry.ImportSubsidiaryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry.SubsidiaryImportType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.SubsidiaryType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal.c;
import ru.gosuslugi.dom.schema.integration.organizations_registry.ImportForeignBranchRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ForeignBranchType;

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
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setCreateSubsidiary (toCreateSubsidiary (r));
        return result;
    }
    
    private static SubsidiaryImportType.CreateSubsidiary toCreateSubsidiary (Map<String, Object> r) {
        final SubsidiaryImportType.CreateSubsidiary result = DB.to.javaBean (SubsidiaryImportType.CreateSubsidiary.class, r);
        result.setSourceName (toSourceName (r));
        result.setOrgVersionGUID (DB.to.String (r.get ("p.orgversionguid")));
        return result;
    }
    
    private static SubsidiaryType.SourceName toSourceName (Map<String, Object> r) {
        final SubsidiaryType.SourceName result = new SubsidiaryType.SourceName ();
        result.setValue (DB.to.String (r.get (c.INFO_SOURCE.lc ())));
        result.setDate (DB.to.XMLGregorianCalendar (DB.to.String (r.get (c.DT_INFO_SOURCE.lc ())).replace (' ', 'T')));
        return result;
    }
    
    public static ImportForeignBranchRequest toImportForeignBranchRequest (Map<String, Object> r) {
        final ImportForeignBranchRequest result = new ImportForeignBranchRequest ();
        result.getForeignBranch ().add (toForeignBranch (r));
        return result;
    }    
    
    private static ImportForeignBranchRequest.ForeignBranch toForeignBranch (Map<String, Object> r) {
        ImportForeignBranchRequest.ForeignBranch result = DB.to.javaBean (ImportForeignBranchRequest.ForeignBranch.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setCreateForeignBranch (toForeignBranchType (r));
        return result;
    }
    
    private static ForeignBranchType toForeignBranchType (Map<String, Object> r) {
        final ForeignBranchType result = DB.to.javaBean (ForeignBranchType.class, r);
        result.setRegistrationCountry ((String) r.get ("oksm.alfa2"));
        return result;
    }
    
    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
            .toMaybeOne (VocOksm.class, "AS oksm", "alfa2").on ()
            .toOne (VocOrganizationProposal.class, "AS r"
                , VocOrganizationProposal.c.ID_ORG_PR_STATUS.lc ()
            ).on ()
            .toOne (VocOrganization.class, "AS p"
                , VocOrganization.c.ORGVERSIONGUID.lc ()
            ).on ("r.parent=p.uuid")
            .toOne (VocOrganization.class, "AS o"
                , VocOrganization.c.ORGPPAGUID.lc ()
            ).on ("r.uuid_org_owner=o.uuid")
            ;
                
    }            
    
}