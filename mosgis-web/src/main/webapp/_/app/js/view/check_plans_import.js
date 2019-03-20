define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'check_plans_import',

                fields : [
                    {name: 'year_from', type: 'text'},
                    {name: 'year_to',   type: 'text'}
                ],

            })
            
       })       

    }

})