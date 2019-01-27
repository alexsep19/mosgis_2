define ([], function () {

    var form_name = 'supply_resource_contract_object_temperature_charts_popup_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'outsidetemperature', type: 'float', options: {min: 0}},
                    {name: 'flowlinetemperature', type: 'float', options: {min: 0}},
                    {name: 'oppositelinetemperature', type: 'float', options: {min: 0}}
                ],

                focus: 0,
            })

       })

    }

})