define ([], function () {

    return function (data, view) {    
        
        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'payment_document_new_form',

                record: data.record,

                fields : [
                    {name: 'paymentdocumentnumber', type: 'text'},
                    {name: 'period',   type: 'list', options: {items: data.periods}},
//                    {name: 'month',   type: 'list', options: {items: data.months}},
//                    {name: 'year',    type: 'list', options: {items: data.years}},
                    {name: 'id_type', type: 'list', options: {items: data.vc_pay_doc_types.items}},
                ],
                
                focus: 3,

            })

       })

    }

})