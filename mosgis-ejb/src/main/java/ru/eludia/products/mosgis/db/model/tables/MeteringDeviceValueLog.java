package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi2;
import ru.gosuslugi.dom.schema.integration.device_metering.ElectricMeteringValueImportType;
import ru.gosuslugi.dom.schema.integration.device_metering.ImportMeteringDeviceValuesRequest;
import ru.gosuslugi.dom.schema.integration.device_metering.OneRateMeteringValueImportType;
import ru.gosuslugi.dom.schema.integration.device_metering.VolumeMeteringValueImportType;

public class MeteringDeviceValueLog extends GisWsLogTable {

    public MeteringDeviceValueLog () {

        super ("tb_meter_values__log", "История редактирования показаний приборов учёта", MeteringDeviceValue.class
            , EnTable.c.class
            , MeteringDeviceValue.c.class
        );

    }
    
    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        final Map<String, Object> r = db.getMap (m                
            .get (MeteringDeviceValueLog.class, id, "*")
            .toOne (MeteringDeviceValue.class, "AS r"
                , EnTable.c.UUID.lc ()
                , MeteringDeviceValue.c.ID_TYPE.lc ()
                , MeteringDeviceValue.c.ID_CTR_STATUS.lc ()
            ).on ()
            .toOne (VocNsi2.class, "code", "guid").on ("r.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1")
            .toOne (MeteringDevice.class, "AS md"
                , MeteringDevice.c.FIASHOUSEGUID.lc () + " AS fiashouseguid"
                , MeteringDevice.c.METERINGDEVICEVERSIONGUID.lc () + " AS meteringdeviceversionguid"
                , MeteringDevice.c.IS_POWER.lc ()
                , MeteringDevice.c.CONSUMEDVOLUME.lc ()
            ).on ()
            .toMaybeOne (VocOrganization.class, "AS org", 
                "orgppaguid AS orgppaguid"
            ).on ("md.uuid_org=org.uuid")
        );
                
        return r;
        
    }    
    
    public static ImportMeteringDeviceValuesRequest toImportMeteringDeviceValuesRequest (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.class, r);
        result.getMeteringDevicesValues ().add (toMeteringDevicesValues (r));
        return result;
    }    
    
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues toMeteringDevicesValues (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.class, r);
        
        if (DB.ok (r.get ("md.consumedvolume"))) {            
            result.setVolumeDeviceValue (toVolumeDeviceValue (r));            
        }
        else {
            
            if (DB.ok (r.get ("md.is_power"))) {
                result.setElectricDeviceValue (toElectricDeviceValue (r));
            }
            else {
                result.setOneRateDeviceValue (toOneRateDeviceValue (r));
            }                
            
        }                
        
        return result;
        
    }
        
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue toVolumeDeviceValue (Map<String, Object> r) {
        
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue.class, r);

        switch (VocMeteringDeviceValueType.i.forId (r.get ("r.id_type"))) {
            case CURRENT:
                result.getCurrentValue ().add (to (r));
                break;
            case CONTROL:
                result.getControlValue ().add (to (r));
                break;
            case BASE:
                throw new IllegalArgumentException ("Base values are not to be sent with this method");
        }
        
        return result;
        
    }    
    
    private static VolumeMeteringValueImportType to (Map<String, Object> r) {
        final VolumeMeteringValueImportType result = DB.to.javaBean (VolumeMeteringValueImportType.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setMunicipalResource (NsiTable.toDom (r, "vc_nsi_2"));
        return result;
    }

    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue toElectricDeviceValue (Map<String, Object> r) {
        
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue.class, r);

        switch (VocMeteringDeviceValueType.i.forId (r.get ("r.id_type"))) {
            case CURRENT:
                result.setCurrentValue (toElectricMeteringValueImportType (r));
                break;
            case CONTROL:
                result.setControlValue (toElectricMeteringValueImportType (r));
                break;
            case BASE:
                throw new IllegalArgumentException ("Base values are not to be sent with this method");
        }
        
        return result;
        
    }
    
    private static ElectricMeteringValueImportType toElectricMeteringValueImportType (Map<String, Object> r) {
        final ElectricMeteringValueImportType result = DB.to.javaBean (ElectricMeteringValueImportType.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
    
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue toOneRateDeviceValue (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue.class, r);
        switch (VocMeteringDeviceValueType.i.forId (r.get ("r.id_type"))) {
            case CURRENT:
                result.getCurrentValue ().add (toOneRateMeteringValueImportType (r));
                break;
            case CONTROL:
                result.getControlValue ().add (toOneRateMeteringValueImportType (r));
                break;
            case BASE:
                throw new IllegalArgumentException ("Base values are not to be sent with this method");
        }
        return result;
    }    
    
    private static OneRateMeteringValueImportType toOneRateMeteringValueImportType (Map<String, Object> r) {
        final OneRateMeteringValueImportType result = DB.to.javaBean (OneRateMeteringValueImportType.class, r);
        result.setMunicipalResource (NsiTable.toDom (r, "vc_nsi_2"));
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }

}