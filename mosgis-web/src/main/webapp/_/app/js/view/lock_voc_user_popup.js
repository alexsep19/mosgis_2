define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'lock_voc_user_form',

                record: data.record,

                fields : [                                
                    {name: 'lockreason', type: 'text'},
                ],

            })

       })

    }

})