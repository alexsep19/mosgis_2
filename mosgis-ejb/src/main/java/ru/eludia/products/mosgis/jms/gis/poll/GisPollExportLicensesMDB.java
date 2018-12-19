package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.License;
import ru.eludia.products.mosgis.db.model.tables.LicenseAccompanyingDocument;
import ru.eludia.products.mosgis.db.model.tables.LicenseHouse;
import ru.eludia.products.mosgis.db.model.tables.LicenseLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocDocumentStatus;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.wsc.WsGisLicenseClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.licenses.AccompanyingDocumentType;
import ru.gosuslugi.dom.schema.integration.licenses.ExportLicenseResultType;
import ru.gosuslugi.dom.schema.integration.licenses.GetStateResult;
import ru.gosuslugi.dom.schema.integration.licenses.LicenseOrganizationType;
import ru.gosuslugi.dom.schema.integration.licenses.LicenseType;
import ru.gosuslugi.dom.schema.integration.licenses_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportLicenseQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportLicensesMDB  extends GisPollMDB {

    @EJB
    protected WsGisLicenseClient wsGisLicenseClient;

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            
            GetStateResult result = getState(r);

            ErrorMessageType errorMessage = result.getErrorMessage();
            if (errorMessage != null) {
                if ("INT002012".equals(errorMessage.getErrorCode())) {

                    logger.warning("License not found");

                    db.update(OutSoap.class, HASH(
                            "uuid", uuid,
                            "id_status", DONE.getId()
                    ));

                    return;
                } else {
                    throw new GisPollException(errorMessage);
                }
            }
            db.begin ();
            
                for (ExportLicenseResultType license : result.getLicense()) {
                    saveLicense(db, uuid, license);
                }

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
            
            db.begin ();

        } catch (GisPollRetryException ex) {
        } catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }
    
    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisLicenseClient.getState ((UUID) r.get("orgppaguid"), (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }
    
    private void saveLicense(DB db, UUID uuidOutSoap, ExportLicenseResultType license) throws SQLException {

        final String uuid = license.getLicenseGUID();

        Map<String, Object> record = HASH(
                License.c.ADDITIONAL_INFORMATION,       license.getAdditionalInformation(),
                License.c.LICENSEABLE_TYPE_OF_ACTIVITY, license.getLicensableTypeOfActivity(),
                License.c.LICENSEGUID,                  uuid,
                License.c.LICENSE_NUMBER,               license.getLicenseNumber(),
                License.c.LICENSE_REG_DATE,             license.getLicenseRegDate(),
                License.c.ID_STATUS,                    VocLicenseStatus.i.forName(license.getLicenseStatus()),
                License.c.LICENSE_VERSION,              license.getLicenseVersion(),
                License.c.REGION_FIAS_GUID,             license.getRegionFIASGuid()
        );

        LicenseOrganizationType organization = license.getLicenseOrganization();
        String licenseOrgOgrn = "";
        String licenseOrgKpp = "";
        if (organization.getEntrp() != null) {
            licenseOrgOgrn = organization.getEntrp().getOGRNIP();
        } else if (organization.getLegal() != null) {
            licenseOrgOgrn = organization.getLegal().getOGRN();
            licenseOrgKpp = organization.getLegal().getKPP();
        }
        record.put(License.c.UUID_ORG.name(), getOrgId(db, licenseOrgOgrn, licenseOrgKpp));

        LicenseType.LicensingAuthority licensingAuthority = license.getLicensingAuthority();
        record.put(License.c.UUID_ORG_AUTHORITY.name(), getOrgId(db, licensingAuthority.getOGRN(), licensingAuthority.getKPP()));

        db.upsert(License.class, record);

        record.put("uuid_object",   uuid);
        record.put("uuid_out_soap", uuidOutSoap);
        record.put("action",        VocAction.i.REFRESH);

        db.insert(LicenseLog.class, record);

        db.dupsert(
                LicenseHouse.class,
                HASH("uuid", uuid),
                license.getHouse().stream().map((l) -> {
                    return HASH(
                            "is_deleted",    0,
                            "fiashouseguid", l.getFIASHouseGUID(),
                            "houseaddress",  l.getHouseAddress()
                    // TODO l.getContract()
                    );
                }).collect(Collectors.toList()),
                "fiashouseguid"
        );
        
        db.delete(db.getModel()
                .select(LicenseAccompanyingDocument.class, "*")
                .where(LicenseAccompanyingDocument.c.UUID_LICENSE.name(), uuid)
        );
        
        List<Map<String, Object>> documents = new ArrayList<>();
        
        for (LicenseType.AccompanyingDocument d : license.getAccompanyingDocument()) {
            AccompanyingDocumentType.BaseDocument baseDocument = d.getBaseDocument();
            AccompanyingDocumentType.Document document = d.getDocument();
            Map<String, Object> r = HASH(
                    LicenseAccompanyingDocument.c.UUID_LICENSE,             uuid,
                    LicenseAccompanyingDocument.c.BASE_DOC_ADDITIONAL_INFO, baseDocument.getAdditionalInfo(),
                    LicenseAccompanyingDocument.c.BASE_DOC_DATE,            baseDocument.getBaseDocDate(),
                    LicenseAccompanyingDocument.c.BASE_DOC_DECISIONORG,     baseDocument.getBaseDocDecisionOrg(),
                    LicenseAccompanyingDocument.c.BASE_DOC_NAME,            baseDocument.getBaseDocName(),
                    LicenseAccompanyingDocument.c.BASE_DOC_NUMBER,          baseDocument.getBaseDocNumber(),
                    LicenseAccompanyingDocument.c.BASE_DOC_DATE_FROM,       baseDocument.getDateFrom(),
                    LicenseAccompanyingDocument.c.DOC_TYPE,                 document.getDocType().getCode(),
                    LicenseAccompanyingDocument.c.ID_DOC_STATUS,            VocDocumentStatus.i.forName(document.getDocumentStatus()).getId(),
                    LicenseAccompanyingDocument.c.NAME,                     document.getName(),
                    LicenseAccompanyingDocument.c.NUM,                      document.getNumber(),
                    LicenseAccompanyingDocument.c.REG_DATE,                 document.getRegDate(),
                    LicenseAccompanyingDocument.c.DATE_FROM,                d.getDateFrom(),
                    LicenseAccompanyingDocument.c.ID_STATUS,                VocDocumentStatus.i.forName(d.getDocumentStatus()).getId(),
                    LicenseAccompanyingDocument.c.UUID_ORG_DECISION,        getOrgId(db, document.getDecisionOrg().getOGRN(), document.getDecisionOrg().getKPP())
            );
            if (baseDocument.getBaseDocType() != null) {
                r.put(LicenseAccompanyingDocument.c.BASE_DOC_TYPE.name(), baseDocument.getBaseDocType().getCode());
            }
        }
        
        db.insert(LicenseAccompanyingDocument.class, documents);
    }
    
    private String getOrgId(DB db, String ogrn, String kpp) throws SQLException {
        Select orgSelect = db.getModel()
                .select(VocOrganization.class, "uuid")
                .where("ogrn", ogrn)
                .and("is_deleted", 0);
        if (StringUtils.isNotBlank(kpp)) {
            orgSelect.and("kpp", kpp);
        }
        String orgUuid = db.getString(orgSelect);
        if (StringUtils.isBlank(orgUuid)) {
            logger.log(Level.SEVERE, "Не найдена организация с ОГРН " + ogrn);

            //TODO Скачивание организации
        }
        return orgUuid;
    }
    
}
