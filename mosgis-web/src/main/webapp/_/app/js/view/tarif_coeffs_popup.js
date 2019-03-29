define ([], function () {

    return function (data, view) {
        var f = 'tarif_coeff_form'

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form input').prop({disabled: !data._can.update})

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'coefficientvalue', type: 'float', options: {min: 0, autoFormat: false}},
                    {name: 'coefficientdescription', type: 'textarea'},
                ],

                focus: 0,
            })

       })

    }

})