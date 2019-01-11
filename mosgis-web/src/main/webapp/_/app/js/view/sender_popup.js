define ([], function () {

    return function (data, view) {
        
        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'sender_form',

                record: data.record,

                fields : [                                
                    {name: 'label', type: 'text'},
                ],

            })

       })

    }

})