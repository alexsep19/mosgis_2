define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'property_document_form',

                record: data.record,

                fields : [                                
                    {name: 'no', type: 'text'},
                    {name: 'dt', type: 'date'},
                    {name: 'label_org_owner', type: 'text'},
                    {name: 'prc', type: 'float', options: {min: 0, max: 100}},
                    {name: 'id_type', type: 'list', options: {items: data.vc_prop_doc_types.items}},
                    {name: 'uuid_premise', type: 'list', options: {items: data.premises}},
                ],
                
                focus: 2,
                
                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#label_org_owner'), $_DO.open_orgs_property_document_org_new)
                
                })}                

            })
            
            if (!data.record.uuid_org_owner) $('#label_org_owner').click ()

       })

    }

})