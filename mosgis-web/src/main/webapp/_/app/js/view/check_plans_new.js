define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'check_plan_form',

                record: data.record,

                fields : [
                    {name: 'year', type: 'text'},
                    {name: 'shouldberegistered', type: 'list', options: {items: [{id:0, text: "Нет"}, {id:1, text: "Да"}]}},
                    {name: 'uriregistrationplannumber', type: 'text'}
                ],

            })

       })

    }

})