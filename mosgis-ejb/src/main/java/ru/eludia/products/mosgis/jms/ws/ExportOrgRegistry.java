package ru.eludia.products.mosgis.jms.ws;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.EntpsType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ForeignBranchType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.LegalType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgVersionType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.SubsidiaryType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.exportOrgRegistry")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOrgRegistry extends WsMDB {

    @Override
    protected JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(
                ExportOrgRegistryRequest.class,
                GetStateResult.class);
    }
        
    @Override
    protected BaseAsyncResponseType generateResponse (DB db, Map<String, Object> r, Object request) throws Fault, SQLException {    
        
        ExportOrgRegistryRequest exportOrgRegistryRequest = (ExportOrgRegistryRequest) request;

        HashMap<String, ExportOrgRegistryResultType> orgRegistryList = new HashMap<>();

        for (ExportOrgRegistryRequest.SearchCriteria searchCriteria : exportOrgRegistryRequest.getSearchCriteria()) {
            
            final Model m = db.getModel();

            Select select = m
                .select     (VocOrganization.class, "AS root", "*")
                .toMaybeOne (VocOrganization.class, "AS parent", "uuid", "orgversionguid").on("root.parent = parent.uuid");
            
            if (StringUtils.isNotBlank(searchCriteria.getOGRNIP())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getOGRNIP());
            } else if (StringUtils.isNotBlank(searchCriteria.getOGRN())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getOGRN());
                if (StringUtils.isNotBlank(searchCriteria.getKPP())) {
                    select.where(VocOrganization.c.KPP, searchCriteria.getKPP());
                }
            } else if (StringUtils.isNotBlank(searchCriteria.getNZA())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getNZA());
            } else if (StringUtils.isNotBlank(searchCriteria.getOrgRootEntityGUID())) {
                select.where(VocOrganization.c.ORGROOTENTITYGUID, searchCriteria.getOrgRootEntityGUID());
            } else if (StringUtils.isNotBlank(searchCriteria.getOrgPPAGUID())) {
                select.where(VocOrganization.c.ORGPPAGUID, searchCriteria.getOrgPPAGUID());
            }

            List<Map<String, Object>> orgsDb = db.getList(select);
            for (Map<String, Object> orgDb : orgsDb) {
                ExportOrgRegistryResultType result = TypeConverter.javaBean(ExportOrgRegistryResultType.class, orgDb);

                ExportOrgRegistryResultType.OrgVersion org = TypeConverter.javaBean(ExportOrgRegistryResultType.OrgVersion.class, orgDb);
                result.setOrgVersion(org);
                
                if (TypeConverter.Boolean(orgDb.get(VocOrganization.c.ISREGISTERED.lc())))
                    result.setIsRegistered(Boolean.TRUE);

                VocOrganizationTypes.i orgType = VocOrganizationTypes.i.forId(orgDb.get(VocOrganization.c.ID_TYPE.lc()));
                switch (orgType) {
                    case ENTPS:
                        orgDb.put("ogrnip", orgDb.get(VocOrganization.c.OGRN.lc()));
                        org.setEntrp(TypeConverter.javaBean(EntpsType.class, orgDb));
                        break;
                    case FOREIGN_BRANCH:
                        orgDb.put("nza", orgDb.get(VocOrganization.c.OGRN.lc()));
                        org.setForeignBranch(TypeConverter.javaBean(ForeignBranchType.class, orgDb));
                        break;
                    case LEGAL:
                        org.setLegal(TypeConverter.javaBean(LegalType.class, orgDb));
                        break;
                    case SUBSIDIARY:
                        ExportOrgRegistryResultType.OrgVersion.Subsidiary subsidiary = TypeConverter.javaBean(ExportOrgRegistryResultType.OrgVersion.Subsidiary.class, orgDb);
                        
                        SubsidiaryType.SourceName source = new SubsidiaryType.SourceName();
                        
                        Object sourceName = orgDb.get(VocOrganization.c.SOURCENAME.lc());
                        Object sourceDate = orgDb.get(VocOrganization.c.SOURCEDATE.lc());
                        if (sourceName != null) source.setValue(sourceName.toString());
                        if (sourceDate != null) source.setDate(DB.to.XMLGregorianCalendar (sourceDate.toString().substring(0, 10)));
                        subsidiary.setSourceName (source);
                        
                        //Головная организация
                        ExportOrgRegistryResultType.OrgVersion.Subsidiary.ParentOrg parentOrg = new ExportOrgRegistryResultType.OrgVersion.Subsidiary.ParentOrg();
                        final RegOrgVersionType regOrgVersion = new RegOrgVersionType ();
                        regOrgVersion.setOrgVersionGUID (orgDb.get ("parent.orgversionguid").toString ());
                        
                        parentOrg.setRegOrgVersion (regOrgVersion);
                        subsidiary.setParentOrg (parentOrg);

                        org.setSubsidiary(subsidiary);
                        break;
                }

                List<Map<String, Object>> roles = db.getList(m
                    .select (VocOrganizationNsi20.class, "AS root")
                    .toOne  (NsiTable.getNsiTable (20), "code", "guid").on ("(root.code = vc_nsi_20.code AND vc_nsi_20.isactual=1)")
                    .where  ("uuid", orgDb.get (VocOrganization.c.UUID.lc()))
                    .and    ("is_deleted", 0)
                );
                
                for (Map<String, Object> role : roles) {
                    result.getOrganizationRoles ().add (NsiTable.toDom (role, "vc_nsi_20"));
                }
                
                orgRegistryList.put (orgDb.get(VocOrganization.c.UUID.lc()).toString(), result);
                
            }
        }
        
        if (orgRegistryList.isEmpty())
            throw new Fault(Errors.INT002012);
            
        GetStateResult result = new GetStateResult();
        result.getExportOrgRegistryResult().addAll(orgRegistryList.values());

        return result;
        
    }

}
