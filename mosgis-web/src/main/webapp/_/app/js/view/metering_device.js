define ([], function () {

    return function (data, view) {
        
        var it = data.item

        var l = data.vc_meter_types [it.id_type]

        it.label = l.charAt (0).toUpperCase () + l.slice (1)
                
        it.status_label = data.vc_gis_status [it.id_ctr_status]
        it.resource_label = data.vc_nsi_2 [it.mask_vc_nsi_2]
        it.volume_label = it.consumedvolume ? 'объём потребленного' : 'текущие показания'

        $('title').text ('ПУ №' + it.meteringdevicenumber)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'metering_device_common', caption: 'Общие'},
                            {id: 'metering_device_docs', caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_metering_device

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'metering_device.active_tab')
            },

        });

        clickOn ($('#house_link'), function () { openTab ('/house/' + data.item ['house.uuid']) })
            
    }

})