define ([], function () {

    return function (data, view) {
    
        var is_virgin = true

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'account_item_popup_form',

                record: data.record,

                fields : [
                    {name: 'fiashouseguid', type: 'list',  options: {items: data.fias}},
                    {name: 'uuid_premise',  type: 'list',  options: {items: []}},
                    {name: 'sharepercent',  type: 'float', options: {min: 0, max: 100, precision: 2}},
                ],
                
                onChange: function (e) {                
                    if (e.target == 'fiashouseguid') e.done ($_DO.load_premises_for_account_item_popup)
                },
                
                onRender: function (e) {                
                    if (!is_virgin) return
                    is_virgin = false                
                    if (data.record.fiashouseguid) e.done ($_DO.load_premises_for_account_item_popup)
                }

            })

       })

    }

})