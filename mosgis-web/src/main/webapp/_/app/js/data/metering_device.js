define ([], function () {

    $_DO.choose_tab_metering_device = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('metering_device.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'metering_devices'}, {}, function (data) {        

            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                vc_meter_types: 1,
                vc_meter_places: 1,
                vc_nsi_2: 1,
                vc_nsi_16: 1,
            })
            
            var it = data.item

            it._can = {cancel: 1}

            if (!it.is_deleted && it.uuid_org == $_USER.uuid_org) {
            
                it._can.edit = 1                            

/*

                switch (it ['ca.id_ctr_status'] || it ['ch.id_ctr_status']) {

                        case 40:
                        case 42:
                        case 43:
                        case 34:
                        case 11:
                        case 92:
                        case 93:
                        case 94:
                        case 100:
                        
                        switch (it.id_ctr_status) {
                            case 10:
                            case 11:
                                it._can.edit = 1                            
                                it._can.approve = 1                            
                        }
                        
                        switch (it.id_ctr_status) {
                            case 14:
                            case 34:
                            case 40:
                                it._can.alter = 1
                        }

                        switch (it.id_ctr_status) {
                            case 10:
                            case 14:
                                it._can.delete = 1
                        }
                        
                }            
                

*/                        
                it._can.update = it._can.edit

            }            
            
            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})