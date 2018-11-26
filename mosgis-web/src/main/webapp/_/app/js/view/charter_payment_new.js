define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'charter_payment_new_form',

                record: data.record,

                fields : [                                
                
                    {name: 'begindate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},                                        
                    {name: 'payment_0', type: 'float', options: {min: 0}},
                    {name: 'payment_1', type: 'float', options: {min: 0}},

                    {name: 'uuid_charter_object', type: 'list', options: {items: data.objects}},

                ],
                
                focus: 2,
                
            })

       })

    }

})