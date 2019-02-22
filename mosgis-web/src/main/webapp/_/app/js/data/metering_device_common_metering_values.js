define ([], function () {

    $_DO.create_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', {
            id_type: 1,
            datevalue: new Date ().toJSON ().slice (0, 10),
            code_vc_nsi_2: e.target.split ('_') [1]
        })

        use.block ('metering_value_popup')

    }

    $_DO.edit_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', w2ui ['metering_device_common_metering_values_grid'].get (e.recid))

        use.block ('metering_value_popup')

    }
    
    $_DO.delete_metering_device_common_metering_values = function (e) {

        if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

        var grid = w2ui ['metering_device_common_metering_values_grid']

        query ({type: 'metering_device_values', id: grid.getSelection () [0], action: 'delete'}, {}, function (d) {
            grid.reload (grid.refresh)
        })

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')

        var it = data.item
                
        add_vocabularies (data, {
            resources: 1,
        })        
        
        done (data)

    }

})