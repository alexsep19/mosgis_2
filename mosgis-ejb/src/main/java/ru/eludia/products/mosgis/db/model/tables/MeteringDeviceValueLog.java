package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.device_metering.ImportMeteringDeviceValuesRequest;

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
                , MeteringDeviceValue.c.ID_CTR_STATUS.lc ()
            ).on ()
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
                result.setOneRateDeviceValue (to (r));
            }                
            
        }                
        
        return result;
        
    }
    
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue toVolumeDeviceValue (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.VolumeDeviceValue.class, r);
        return result;
    }    
    
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue toElectricDeviceValue (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.ElectricDeviceValue.class, r);
        return result;
    }
    
    private static ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue to (Map<String, Object> r) {
        final ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue result = DB.to.javaBean (ImportMeteringDeviceValuesRequest.MeteringDevicesValues.OneRateDeviceValue.class, r);
        return result;
    }    

}