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
                vc_meter_file_types: 1,
                vc_meter_value_types: 1,
            })
            
            data.accs = dia2w2uiRecords (data.accs)

            $.each (data.accs, function () {
                this.recid = this.id = this ['acc.uuid']
                this.label = this ['ind.label'] || this ['org.label']
            })


            data.meters = dia2w2uiRecords (data.meters)

            $.each (data.meters, function () {
                this.recid = this.id = this.uuid_meter
            })

            var it = data.item

            it._can = {cancel: 1}

            if (!it.is_deleted && it.uuid_org == $_USER.uuid_org) {
            
                it._can.edit_values = 1

                switch (it.id_ctr_status) {
                    case 10:
                    case 11:
                        it._can.edit = 1
                        it._can.approve = 1
                }

                switch (it.id_ctr_status) {
                    case 10:
                    case 14:
                        it._can.delete = 1
                }
                
                switch (it.id_ctr_status) {
                    case 14:
                    case 34:
                    case 40:
                        it._can.alter = 1
                }

                it._can.update = it._can.edit

            }

            data.resources = []

            if (it._can.edit_values) {

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

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})