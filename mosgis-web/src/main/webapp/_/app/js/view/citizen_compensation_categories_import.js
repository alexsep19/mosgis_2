define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'citizen_compensation_categories_import',

                fields : [
                    {name: 'fromdate', type: 'date'},
                    {name: 'todate',   type: 'date'}
                ],

            })
            
       })       

    }

})