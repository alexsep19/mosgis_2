define ([], function () {

    $_DO.create_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', {
            uuid_meter: $_REQUEST.id,
            code_vc_nsi_2: e.target.split ('_') [1]
        })

        use.block ('metering_value_popup')

    }

    $_DO.edit_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', w2ui ['metering_device_common_metering_values_grid'].get (e.recid))

        use.block ('metering_value_popup')

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')

        var it = data.item
        
        data.resources = []
                
        if (it._can.edit) {
        
            var m = 1

            for (var i = 1; i < 6; i ++) {
                
                if (m & it.mask_vc_nsi_2) data.resources.push ({
                    id: i,
                    label: data.vc_nsi_2 [m]
                })

                m = m << 1

            }

        }    
        
        if (data.resources.length == 1) data.resources [0].label = 'Внести показания'
        
        add_vocabularies (data, {
            resources: 1,
        })        
        
        done (data)

    }

})