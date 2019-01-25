define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_organization_import_popup_form',

                record: data.record,

                fields : [                                
                    {name: 'import_type', type: 'list', options: {items: data.import_types}},
                ],

            })

       })

    }

})