define ([], function () {
    
    $_DO.create_house_metering_devices = function (e) {
            
        use.block ('metering_device_new')
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        if (!it._can) it._can = {}
        
        if (data.cach && data.cach ['org.uuid'] == $_USER.uuid_org) {
        
            it._can.add_metering_devices = 1
            
        }

        if (data.srca && data.srca ['org.uuid'] == $_USER.uuid_org) {
        
            it._can.add_metering_devices = 1
            
        }
        
        query ({type: 'metering_devices', id: null, part: 'vocs'}, {}, function (d) {
        
//            var meter_types = d.vc_meter_types
                    
            add_vocabularies (d, d)
            
            for (k in d) data [k] = d [k]
            
//            data.meter_types = meter_types
        
            done (data);

        })

    }

})