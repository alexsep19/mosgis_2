define ([], function () {

    return function (data, view) {
        
        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                    {name: 'f', type: 'text'},
                    {name: 'i', type: 'text'},
                    {name: 'o', type: 'text'},
                    {name: 'login', type: 'text'},
                ],

            })

       })

    }

})