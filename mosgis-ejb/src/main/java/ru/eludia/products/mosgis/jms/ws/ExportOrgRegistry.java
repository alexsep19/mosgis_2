package ru.eludia.products.mosgis.jms.ws;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.util.XmlUtils;
import ru.eludia.products.mosgis.web.base.Errors;
import ru.eludia.products.mosgis.web.base.Fault;
import ru.mos.gkh.gis.schema.integration.base.BaseAsyncResponseType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.EntpsType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.ForeignBranchType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.LegalType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.RegOrgType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.SubsidiaryType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.ExportOrgRegistryResultType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.GetStateResult;

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
    protected BaseAsyncResponseType handleRequest(DB db, Object request) throws Exception {
        try {
            return generateResponse(db, request);
        } catch (Fault e) {
            GetStateResult result = new GetStateResult();
            result.setErrorMessage(createErrorMessage(e));
            return result;
        }
    }
        
    private BaseAsyncResponseType generateResponse(DB db, Object request) throws Fault, SQLException {    
        
        ExportOrgRegistryRequest exportOrgRegistryRequest = (ExportOrgRegistryRequest) request;

        HashMap<String, ExportOrgRegistryResultType> orgRegistryList = new HashMap<>();

        for (ExportOrgRegistryRequest.SearchCriteria searchCriteria : exportOrgRegistryRequest.getSearchCriteria()) {

            Select select = db.getModel().select(VocOrganization.class, "AS root", "*")
                    .toMaybeOne(VocOrganization.class, "AS parent", "uuid").on("root.parent = parent.uuid");
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

                ExportOrgRegistryResultType.Org org = TypeConverter.javaBean(ExportOrgRegistryResultType.Org.class, orgDb);
                result.setOrg(org);
                
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
                        ExportOrgRegistryResultType.Org.Subsidiary subsidiary = TypeConverter.javaBean(ExportOrgRegistryResultType.Org.Subsidiary.class, orgDb);
                        
                        SubsidiaryType.SourceName source = new SubsidiaryType.SourceName();
                        
                        Object sourceName = orgDb.get(VocOrganization.c.SOURCENAME.lc());
                        Object sourceDate = orgDb.get(VocOrganization.c.SOURCEDATE.lc());
                        if (sourceName != null) source.setValue(sourceName.toString());
                        if (sourceDate != null) source.setDate(LocalDate.parse(sourceDate.toString().substring(0, 10)));
                        subsidiary.setSourceName(source);
                        
                        //Головная организация
                        RegOrgType regOrg = new RegOrgType();
                        regOrg.setOrgRootEntityGUID(orgDb.get("parent.uuid").toString());
                        ExportOrgRegistryResultType.Org.Subsidiary.ParentOrg parentOrg = new ExportOrgRegistryResultType.Org.Subsidiary.ParentOrg();
                        parentOrg.setRegOrg(regOrg);
                        subsidiary.setParentOrg(parentOrg);

                        org.setSubsidiary(subsidiary);
                        break;
                }

                List<Map<String, Object>> roles = db.getList(db.getModel()
                        .select(VocOrganizationNsi20.class, "*")
                        .where("uuid", orgDb.get(VocOrganization.c.UUID.lc()))
                        .and("is_deleted", 0));
                for (Map<String, Object> role : roles) {
                    result.getOrganizationRoles().add(XmlUtils.createWsNsiRef(20, role.get("code").toString()));
                }
                orgRegistryList.put(orgDb.get(VocOrganization.c.UUID.lc()).toString(), result);
            }
        }
        
        if (orgRegistryList.isEmpty())
            throw new Fault(Errors.INT002012);
            
        GetStateResult result = new GetStateResult();
        result.getExportOrgRegistryResult().addAll(orgRegistryList.values());

        return result;
    }

}
