define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'charter_payment_common_service_payments_popup_form',

                fields : [                                
                    {name: 'description',  type: 'textarea'},
                    {name: 'files', type: 'file', options: {max: 1, maxWidth: 290}},
                ],

            })

       })       

    }

})