define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'account_item_popup_form',

                record: data.record,

                fields : [
                    {name: 'fiashouseguid', type: 'list',  options: {items: data.fias}},
                    {name: 'uuid_premise',  type: 'list',  options: {items: []}},
                    {name: 'sharepercent',  type: 'float', options: {min: 0, max: 100, precision: 2}},
                ],

            })

       })

    }

})