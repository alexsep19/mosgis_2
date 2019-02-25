define ([], function () {

    $_DO.update_metering_device_metering_devices_popup = function (e) {

        query (

            {type: 'metering_devices', id: $_REQUEST.id, action: 'set_meters'},

            {data: {uuid_meter: w2ui ['metering_devices_grid'].getSelection ()}},

            reload_page

        )

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        var v = {fiashouseguid: null, uuid_org: null}
        
        for (k in v) v [k] = it [k]        
                
        query ({type: 'metering_devices', id: null}, {data: v, offset: 0, limit: 1000000}, function (d) {
        
            data.metering_devices = []
            
            $.each (d.root, function () {
            
                if (this.uuid_premise) return
                if (!this.installationplace) return
                if (this.uuid == $_REQUEST.id) return
            
                this.recid = this.id
                            
                data.metering_devices.push (this)
                
            })

            data.record = {}

            done (data)
        
        })        

    }

})