define ([], function () {

    var form_name = 'citizen_compensation_calc_popup_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'periodfrom', type: 'date'},
                    {name: 'periodto', type: 'date'},
                    {name: 'calculationdate', type: 'date'},
                    {name: 'compensationsum', type: 'float', options: {min: 0}},
                ],

                focus: data.record.id? -1 : 0
            })
       })

    }

})