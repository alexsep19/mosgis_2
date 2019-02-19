define ([], function () {

    var name = 'metering_device_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                                
                
                    {name: 'is_volume', type: 'list', options: {items: [
                        {id: 0, text: 'текущие показания'},
                        {id: 1, text: 'потреблённый объём'},
                    ]}},

                    {name: 'id_type', type: 'list', options: {items: data.types}},
                    {name: 'mask_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2.items}},
                    
                    {name: 'meteringdevicenumber', type: 'text'},
                    {name: 'meteringdevicestamp', type: 'text'},
                    {name: 'meteringdevicemodel', type: 'text'},
                    
                ],
                
//                focus: -1,
                                                        
            })

       })

    }

})