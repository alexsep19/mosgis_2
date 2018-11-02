define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'property_document_form',

                record: data.record,

                fields : [
                    {name: 'no', type: 'text'},
                    {name: 'dt', type: 'date'},
                    {name: 'prc', type: 'float', options: {min: 0, max: 100}},
                    {name: 'id_type', type: 'list', options: {items: data.vc_prop_doc_types.items}},
                    {name: 'uuid_premise', type: 'list', options: {items: data.premises}},
                    {name: 'uuid_person_owner', type: 'list', options: {items: data.vc_persons}},
                ],

                focus: 0,

                onRefresh: function (e) {e.done (function () {

                    clickOn ($('#label_person_owner'), $_DO.open_persons_property_document_person_new)

                })}                

            })

            if (!data.record.uuid_person_owner) $('#label_person_owner').click ()

       })

    }

})