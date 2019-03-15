define ([], function () {
/*    
    $_DO.create_metering_devices = function (e) {
            
        use.block ('metering_device_new')
    
    }
*/    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = {}
                
        query ({type: 'metering_devices', id: null, part: 'vocs'}, {}, function (d) {
                            
            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            done (data);

        })

    }

})