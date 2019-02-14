package ru.eludia.products.mosgis.jms.ws;

import java.time.LocalDate;
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
import ru.mos.gkh.gis.schema.integration.base.BaseAsyncResponseType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.EntpsType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.ForeignBranchType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.LegalType;
import ru.mos.gkh.gis.schema.integration.organizations_registry_base.RegOrgVersionType;
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
        ExportOrgRegistryRequest exportOrgRegistryRequest = (ExportOrgRegistryRequest) request;

        HashMap<String, ExportOrgRegistryResultType> orgRegistryList = new HashMap<>();

        LocalDate lastEditingDateFrom = exportOrgRegistryRequest.getLastEditingDateFrom(); //TODO ??

        for (ExportOrgRegistryRequest.SearchCriteria searchCriteria : exportOrgRegistryRequest.getSearchCriteria()) {

            Select select = db.getModel().select(VocOrganization.class, "*");
            if (StringUtils.isNotBlank(searchCriteria.getOGRNIP())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getOGRNIP());
            } else if (StringUtils.isNotBlank(searchCriteria.getOGRN())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getOGRN());
                if (StringUtils.isNotBlank(searchCriteria.getKPP())) {
                    select.where(VocOrganization.c.KPP, searchCriteria.getKPP());
                }
            } else if (StringUtils.isNotBlank(searchCriteria.getNZA())) {
                select.where(VocOrganization.c.OGRN, searchCriteria.getNZA());
            } else if (StringUtils.isNotBlank(searchCriteria.getOrgVersionGUID())) {
                select.where(VocOrganization.c.ORGVERSIONGUID, searchCriteria.getOrgVersionGUID());
            } else if (StringUtils.isNotBlank(searchCriteria.getOrgRootEntityGUID())) {
                select.where(VocOrganization.c.ORGROOTENTITYGUID, searchCriteria.getOrgRootEntityGUID());
            } else if (StringUtils.isNotBlank(searchCriteria.getOrgPPAGUID())) {
                select.where(VocOrganization.c.ORGPPAGUID, searchCriteria.getOrgPPAGUID());
            }
            searchCriteria.isIsRegistered(); //TODO ??

            List<Map<String, Object>> orgs = db.getList(select);
            for (Map<String, Object> org : orgs) {
                ExportOrgRegistryResultType result = TypeConverter.javaBean(ExportOrgRegistryResultType.class, org);

                ExportOrgRegistryResultType.OrgVersion orgVersion = TypeConverter.javaBean(ExportOrgRegistryResultType.OrgVersion.class, org);
                result.setOrgVersion(orgVersion);

                result.setIsRegistered(Boolean.TRUE); //TODO ??
                orgVersion.setIsActual(true); //TODO ??
                orgVersion.setRegistryOrganizationStatus("P"); //TODO ??
                orgVersion.setLastEditingDate(LocalDate.now()); //TODO ??

                VocOrganizationTypes.i orgType = VocOrganizationTypes.i.forId(org.get(VocOrganization.c.ID_TYPE.lc()));
                switch (orgType) {
                    case ENTPS:
                        org.put("ogrnip", org.get(VocOrganization.c.OGRN.lc()));
                        orgVersion.setEntrp(TypeConverter.javaBean(EntpsType.class, org));
                        break;
                    case FOREIGN_BRANCH:
                        org.put("nza", org.get(VocOrganization.c.OGRN.lc()));
                        orgVersion.setForeignBranch(TypeConverter.javaBean(ForeignBranchType.class, org));
                        break;
                    case LEGAL:
                        orgVersion.setLegal(TypeConverter.javaBean(LegalType.class, org));
                        break;
                    case SUBSIDIARY:
                        ExportOrgRegistryResultType.OrgVersion.Subsidiary subsidiary = TypeConverter.javaBean(ExportOrgRegistryResultType.OrgVersion.Subsidiary.class, org);
                        subsidiary.setStatusVersion("P"); //TODO ??

                        //Головная организация
                        RegOrgVersionType regOrgVersion = new RegOrgVersionType();
                        regOrgVersion.setOrgVersionGUID(UUID.randomUUID().toString()); //TODO Доработать
                        ExportOrgRegistryResultType.OrgVersion.Subsidiary.ParentOrg parentOrg = new ExportOrgRegistryResultType.OrgVersion.Subsidiary.ParentOrg();
                        parentOrg.setRegOrgVersion(regOrgVersion);
                        subsidiary.setParentOrg(parentOrg);

                        orgVersion.setSubsidiary(subsidiary);
                        break;
                }

                List<Map<String, Object>> roles = db.getList(db.getModel()
                        .select(VocOrganizationNsi20.class, "*")
                        .where("uuid", org.get(VocOrganization.c.UUID.lc()))
                        .and("is_deleted", 0));
                for (Map<String, Object> role : roles) {
                    result.getOrganizationRoles().add(XmlUtils.createWsNsiRef(20, role.get("code").toString()));
                }
                orgRegistryList.put(org.get(VocOrganization.c.UUID.lc()).toString(), result);
            }
        }

        GetStateResult result = new GetStateResult();
        result.getExportOrgRegistryResult().addAll(orgRegistryList.values());

        return result;
    }

}
