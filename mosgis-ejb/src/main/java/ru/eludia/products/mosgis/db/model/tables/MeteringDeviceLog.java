package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi2;
import ru.gosuslugi.dom.schema.integration.house_management.ImportMeteringDeviceDataRequest;
import ru.gosuslugi.dom.schema.integration.house_management.MeteringDeviceBasicCharacteristicsType;
import ru.gosuslugi.dom.schema.integration.house_management.MeteringDeviceFullInformationType;
import ru.gosuslugi.dom.schema.integration.house_management.MunicipalResourceElectricBaseType;

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
            ).on ()
        );

        r.put ("tb_meter_values", db.getList (m                
            .select (MeteringDeviceValue.class, "AS root", "*")
            .where (EnTable.c.IS_DELETED, 0)
            .where (MeteringDeviceValue.c.UUID_METER, r.get ("r.uuid"))
            .where (MeteringDeviceValue.c.ID_TYPE, VocMeteringDeviceValueType.i.BASE.getId ())
            .toOne (VocNsi2.class, "code", "guid").on ("root.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1")
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
        }
        else {

            if (DB.eq (
                r.get (MeteringDevice.c.MASK_VC_NSI_2.lc ())
                , Nsi2.i.POWER.getId ()
            )) {                                                                // MunicipalResourceEnergy
                result.setMunicipalResourceEnergy (toMunicipalResourceElectricBaseType (r));
            }
            else {                                                              // MunicipalResourceNotEnergy
            }

        }
        
        return result;
        
    }
    
    private static MunicipalResourceElectricBaseType toMunicipalResourceElectricBaseType (Map<String, Object> r) {
        final MunicipalResourceElectricBaseType result = DB.to.javaBean (MunicipalResourceElectricBaseType.class, r);
        return result;
    }    
    
    private static MeteringDeviceFullInformationType.LinkedWithMetering toLinkedWithMetering (Map<String, Object> r) {
        final MeteringDeviceFullInformationType.LinkedWithMetering result = DB.to.javaBean (MeteringDeviceFullInformationType.LinkedWithMetering.class, r);
        return result;
    }
    
    private static MeteringDeviceBasicCharacteristicsType toMeteringDeviceBasicCharacteristicsType (Map<String, Object> r) {
        
        final MeteringDeviceBasicCharacteristicsType result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.class, r);
        
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
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.CollectiveDevice toCollectiveDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.CollectiveDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.CollectiveDevice.class, r);
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice toCollectiveApartmentDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.CollectiveApartmentDevice.class, r);
        return result;
    }

    private static MeteringDeviceBasicCharacteristicsType.LivingRoomDevice toLivingRoomDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.LivingRoomDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.LivingRoomDevice.class, r);
        return result;
    }
    
    private static MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice toResidentialPremiseDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.ResidentialPremiseDevice.class, r);
        result.getPremiseGUID ().add (DB.to.String (r.get ("premiseguid")));
        return result;
    }
    
    private static MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice toNonResidentialPremiseDevice (Map<String, Object> r) {
        final MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice result = DB.to.javaBean (MeteringDeviceBasicCharacteristicsType.NonResidentialPremiseDevice.class, r);
        result.getPremiseGUID ().add (DB.to.String (r.get ("premiseguid")));
        return result;
    }
        
}