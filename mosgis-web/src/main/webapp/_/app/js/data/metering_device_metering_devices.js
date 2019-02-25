define ([], function () {

    $_DO.create_metering_device_metering_devices = function (e) {

        use.block ('metering_device_metering_devices_popup')

    }
    
    $_DO.delete_metering_device_metering_devices = function (e) {
    
        if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

        query (

            {type: 'metering_devices', id: $_REQUEST.id, action: 'unset_meters'},

            {data: {uuid_meter: w2ui ['metering_device_metering_devices_grid'].getSelection ()}},

            reload_page

        )
    
    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))

        done (data)

    }

})