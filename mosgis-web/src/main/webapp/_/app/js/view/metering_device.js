define ([], function () {

    return function (data, view) {
        
        var it = data.item

        var l = data.vc_meter_types [it.id_type]

        it.label = l.charAt (0).toUpperCase () + l.slice (1)
                
        $('title').text ('ПУ №' + it.meteringdevicenumber)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'metering_device_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_metering_device

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'metering_device.active_tab')
            },

        });

    }

})