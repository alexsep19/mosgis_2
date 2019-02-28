package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceFileType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi2;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMunicipalResourceType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportMeteringDeviceDataRequest;
import ru.gosuslugi.dom.schema.integration.house_management.MeteringDeviceBasicCharacteristicsType;
import ru.gosuslugi.dom.schema.integration.house_management.MeteringDeviceFullInformationType;
import ru.gosuslugi.dom.schema.integration.house_management.MunicipalResourceElectricBaseType;
import ru.gosuslugi.dom.schema.integration.house_management.MunicipalResourceNotElectricBaseType;

public class MeteringDeviceLog extends GisWsLogTable {

    public MeteringDeviceLog () {

        super ("tb_meters__log", "История редактирования приборов учёта", MeteringDevice.class
            , EnTable.c.class
            , MeteringDevice.c.class
        );

    }
    
    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        final Map<String, Object> r = db.getMap (m                
            .get (MeteringDeviceLog.class, id, "*")
            .toOne (MeteringDevice.class, "AS r"
                , EnTable.c.UUID.lc ()
                , MeteringDevice.c.FIASHOUSEGUID.lc () + " AS fiashouseguid"
                , MeteringDevice.c.ID_CTR_STATUS.lc ()
                , MeteringDevice.c.ID_TYPE.lc ()
            ).on ()
            .toMaybeOne (VocOrganization.class, "AS org", 
                "orgppaguid AS orgppaguid"
            ).on ("r.uuid_org=org.uuid")
            .toMaybeOne (Premise.class
                , Premise.c.PREMISESGUID.lc () + " AS premiseguid"
                , Premise.c.LIVINGROOMGUID.lc () + " AS livingroomguid"
            ).on ()
            .toMaybeOne (VocNsi16.class, "code", "guid").on ("r.code_vc_nsi_16=vc_nsi_16.code AND vc_nsi_16.isactual=1")
                
        );
        
        final Object uuidMeter = r.get ("r.uuid");
        
        if (DB.ok (r.get (MeteringDevice.c.CONSUMEDVOLUME.lc ()))) {
            
            r.put ("values", Collections.EMPTY_LIST);
            
            r.put ("nsi_2", db.getList (m
                .select (Nsi2.class
                    , Nsi2.c.CODE.lc () + " AS vc_nsi_2.code"
                    , Nsi2.c.GUID.lc () + " AS vc_nsi_2.guid"
                )
                .where (Nsi2.c.CODE.lc () + " IN", Nsi2.i.forId (r.get (MeteringDevice.c.MASK_VC_NSI_2.lc ())).getCodes ())
            ));
            
        }
        else {
            
            r.put ("nsi_2", Collections.EMPTY_LIST);
            
            r.put ("values", db.getList (m
                .select (MeteringDeviceValue.class, "AS root"
                    , MeteringDeviceValue.c.METERINGVALUE.lc ()
                    , MeteringDeviceValue.c.METERINGVALUET1.lc ()
                    , MeteringDeviceValue.c.METERINGVALUET2.lc ()
                    , MeteringDeviceValue.c.METERINGVALUET3.lc ()
                )
                .where (EnTable.c.IS_DELETED, 0)
                .where (MeteringDeviceValue.c.UUID_METER, uuidMeter)
                .where (MeteringDeviceValue.c.ID_TYPE, VocMeteringDeviceValueType.i.BASE.getId ())
                .toOne (VocNsi2.class, "code", "guid").on ("root.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1")
            ));
            
        }

        r.put ("accountguid", db.getList (m
            .select (MeteringDeviceAccount.class, "AS root")
            .where (EnTable.c.UUID, uuidMeter)
            .toOne (Account.class
                , Account.c.ACCOUNTGUID.lc () + " AS accountguid"
            ).on ()
        ).stream ().map (t -> t.get ("accountguid").toString ()).collect (Collectors.toList ()));

        r.put ("files", db.getList (m
            .select (MeteringDeviceFile.class, "*")
            .toOne  (MeteringDeviceFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            .where (MeteringDeviceFile.c.UUID_METER, uuidMeter)
            .where (AttachTable.c.ID_STATUS, VocFileStatus.i.LOADED.getId ())
        ));

        return r;

    }

    public static ImportMeteringDeviceDataRequest toImportMeteringDeviceData (Map<String, Object> r) {
        final ImportMeteringDeviceDataRequest result = DB.to.javaBean (ImportMeteringDeviceDataRequest.class, r);
        result.getMeteringDevice ().add (toMeteringDevice (r));
        return result;
    }        
    
    private static ImportMeteringDeviceDataRequest.MeteringDevice toMeteringDevice (Map<String, Object> r) {
        final ImportMeteringDeviceDataRequest.MeteringDevice result = DB.to.javaBean (ImportMeteringDeviceDataRequest.MeteringDevice.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setDeviceDataToCreate (toMeteringDeviceFullInformationType (r));
        return result;
    }
    
    private static MeteringDeviceFullInformationType toMeteringDeviceFullInformationType (Map<String, Object> r) {
        
        final MeteringDeviceFullInformationType result = DB.to.javaBean (MeteringDeviceFullInformationType.class, r);
        
        result.setBasicChatacteristicts (toMeteringDeviceBasicCharacteristicsType (r));
                
        if (!Boolean.TRUE.equals (result.isNotLinkedWithMetering ())) {
            result.setNotLinkedWithMetering (null);
            result.setLinkedWithMetering (toLinkedWithMetering (r));
        }
        
        if (DB.ok (r.get (MeteringDevice.c.CONSUMEDVOLUME.lc ()))) {            // MunicipalResources 
            
            for (Object i: (List) r.get ("nsi_2")) {
                Map<String, Object> n = (Map<String, Object>) i;
                n.putAll (r);
                result.getMunicipalResources ().add (toDeviceMunicipalResourceType (n));
            }
            
        }
        else {

            if (DB.eq (
                r.get (MeteringDevice.c.MASK_VC_NSI_2.lc ())
                , Nsi2.i.POWER.getId ()
            )) {                                                                // MunicipalResourceEnergy
                result.setMunicipalResourceEnergy (toMunicipalResourceElectricBaseType (r));
            }
            else {                                                              // MunicipalResourceNotEnergy
                for (Object i: (List) r.get ("values")) result.getMunicipalResourceNotEnergy ().add (toMunicipalResourceNotElectricBaseType (i));
            }

        }
        
        return result;
        
    }
    
    private static MunicipalResourceElectricBaseType toMunicipalResourceElectricBaseType (Map<String, Object> r) {
        r.putAll ((Map) ((List) r.get ("values")).get (0));
        final MunicipalResourceElectricBaseType result = DB.to.javaBean (MunicipalResourceElectricBaseType.class, r);
        return result;
    }    
    
    private static MeteringDeviceFullInformationType.LinkedWithMetering toLinkedWithMetering (Map<String, Object> r) {
        final MeteringDeviceFullInformationType.LinkedWithMetering result = DB.to.javaBean (MeteringDeviceFullInformationType.LinkedWithMetering.class, r);
        return result;
    }
    
    private static MeteringDeviceBasicCharacteristicsType toMeteringDeviceBasicCharacteristicsType (Map<String, Object> r) {
        
        final MeteringDeviceBasicCharacteristicsType result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.class, r);
        
        if (DB.ok (r.get ("code_vc_nsi_16"))) result.setVerificationInterval (NsiTable.toDom (r, "vc_nsi_16"));
        
        switch (VocMeteringDeviceType.i.forId (r.get ("r.id_type"))) {
            case APARTMENT_HOUSE:
                result.setApartmentHouseDevice (toApartmentHouseDevice (r));
                break;
            case COLLECTIVE:
                result.setCollectiveDevice (toCollectiveDevice (r));
                break;
            case COLLECTIVE_APARTMENT:
                result.setCollectiveApartmentDevice (toCollectiveApartmentDevice (r));
                break;
            case LIVING_ROOM:
                result.setLivingRoomDevice (toLivingRoomDevice (r));
                break;
            case NON_RESIDENTIAL_PREMISE:
                result.setNonResidentialPremiseDevice (toNonResidentialPremiseDevice (r));
                break;
            case RESIDENTIAL_PREMISE:
                result.setResidentialPremiseDevice (toResidentialPremiseDevice (r));
                break;
        }
                
        return result;
        
    }

    private static MeteringDeviceBasicCharacteristicsType.ApartmentHouseDevice toApartmentHouseDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.ApartmentHouseDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.ApartmentHouseDevice.class, r);
        for (Object i: (List) r.get ("files")) result.getCertificate ().add (AttachTable.toAttachmentType ((Map<String, Object>) i)); 
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.CollectiveDevice toCollectiveDevice (Map<String, Object> r) {
        
        final MeteringDeviceBasicCharacteristicsType.CollectiveDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.CollectiveDevice.class, r);

        for (Object i: (List) r.get ("files")) {            
            final Map<String, Object> file = (Map<String, Object>) i;            
            final AttachmentType a = AttachTable.toAttachmentType (file);                    
            switch (VocMeteringDeviceFileType.i.forId (file.get (MeteringDeviceFile.c.ID_TYPE.lc ()))) {                
                case CERTIFICATE:
                    result.getCertificate ().add (a);
                    break;
                case PROJECT_REGISTRATION_NODE:
                    result.getProjectRegistrationNode ().add (a);
                    break;                    
            }                        
        }
        
        return result;
        
    }

    private static MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice toCollectiveApartmentDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice.class, r);
        for (Object i: (List) r.get ("files")) result.getCertificate ().add (AttachTable.toAttachmentType ((Map<String, Object>) i)); 
        result.getPremiseGUID ().add (DB.to.String (r.get ("premiseguid")));
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.LivingRoomDevice toLivingRoomDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.LivingRoomDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.LivingRoomDevice.class, r);
        result.getLivingRoomGUID ().add (DB.to.String (r.get ("livingroomguid")));
        for (Object i: (List) r.get ("files")) result.getCertificate ().add (AttachTable.toAttachmentType ((Map<String, Object>) i)); 
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice toResidentialPremiseDevice (Map<String, Object> r) {
        r.putAll ((Map) ((List) r.get ("values")).get (0));
        final MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice.class, r);
        result.getPremiseGUID ().add (DB.to.String (r.get ("premiseguid")));
        for (Object i: (List) r.get ("files")) result.getCertificate ().add (AttachTable.toAttachmentType ((Map<String, Object>) i)); 
        return result;
    }
    
    private static MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice toNonResidentialPremiseDevice (Map<String, Object> r) {
        r.putAll ((Map) ((List) r.get ("values")).get (0));
        final MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice.class, r);
        result.getPremiseGUID ().add (DB.to.String (r.get ("premiseguid")));
        for (Object i: (List) r.get ("files")) result.getCertificate ().add (AttachTable.toAttachmentType ((Map<String, Object>) i)); 
        return result;
    }
    
    private static MunicipalResourceNotElectricBaseType toMunicipalResourceNotElectricBaseType (Object i) {
        final Map<String, Object> r = (Map<String, Object>) i;
        final MunicipalResourceNotElectricBaseType result = DB.to.javaBean (MunicipalResourceNotElectricBaseType.class, r);
        result.setMunicipalResource (NsiTable.toDom (r, "vc_nsi_2"));
        return result;
    }    
    
    private static DeviceMunicipalResourceType toDeviceMunicipalResourceType (Map<String, Object> r) {
        
        final DeviceMunicipalResourceType result = DB.to.javaBean (DeviceMunicipalResourceType.class, r);
        
        result.setMunicipalResource (NsiTable.toDom (r, "vc_nsi_2"));
        
        if (!DB.eq (
            result.getMunicipalResource ().getCode (),
            Nsi2.i.POWER.getCode ()
        )) {
            result.setTariffCount (null);
            result.setTransformationRatio (null);
        }
        
        return result;
        
    }
        
}