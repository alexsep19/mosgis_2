define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'agreement_payment_popup_form',

                record: data.record,

                fields : [                                
                
                    {name: 'datefrom', type: 'date', options: {
                        keyboard: false,
//                        start:    dt_dmy (data.item.effectivedate),
//                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},
                    {name: 'dateto',   type: 'date', options: {
                        keyboard: false,
//                        start:    dt_dmy (data.item.effectivedate),
//                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},                                        
                    {name: 'bill', type: 'float', options: {precision: 2}},
                    {name: 'debt', type: 'float', options: {precision: 2}},
                    {name: 'paid', type: 'float', options: {precision: 2}},

                ],
                
                focus: 2,
                
            })

       })

    }

})