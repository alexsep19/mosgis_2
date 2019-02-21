define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'metering_device_common_service_payments_popup_form',
                
                record: {id_type: 1},

                fields : [                                
                    {name: 'description',  type: 'textarea'},
                    {name: 'files', type: 'file', options: {max: 1, maxWidth: 290}},
                    {name: 'id_type', type: 'list', options: {items: data.vc_meter_file_types.items}},
                ],

            })

       })       

    }

})