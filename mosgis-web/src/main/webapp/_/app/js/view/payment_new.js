define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'payment_new_form',

                record: data.record,

                fields : [
                    {name: 'id_type', type: 'int'},
                    {name: 'uuid_account', type: 'text'},
                    {name: 'uuid_pay_doc', type: 'text'},
                    {name: 'ordernum', type: 'text'},
                    {name: 'orderdate', type: 'date'},
                    {name: 'amount', type: 'float', options: {min: 0, precision: 2}},
                    {name: 'period',   type: 'list', options: {items: data.periods}},
                ],

                focus: 3,

            })

       })

    }

})