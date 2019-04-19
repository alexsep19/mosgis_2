define ([], function () {

    var form_name = 'citizen_compensation_payment_popup_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'paymentdate', type: 'date'},
                    {name: 'paymenttype', type: 'list', options: {items: data.vc_cit_comp_pay_types.items}},
                    {name: 'paymentsum', type: 'float', options: {min: 0}},
                ],

                focus: data.record.id? -1 : 0
            })
       })

    }

})