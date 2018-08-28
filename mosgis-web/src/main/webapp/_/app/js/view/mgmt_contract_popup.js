define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'mgmt_contract_form',

                record: data.record,

                fields : [                                
                    {name: 'docnum', type: 'text'},
                    {name: 'signingdate', type: 'date'},
                    {name: 'effectivedate', type: 'date'},
                    {name: 'plandatecomptetion', type: 'date'},
                    {name: 'code_vc_nsi_58', type: 'list', options: {items: data.vc_nsi_58.items}},
                    {name: 'uuid_org_customer', type: 'hidden'},
                    {name: 'label_org_customer', type: 'text'},
                ],
                
                focus: 2,
                
                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#label_org_customer'), $_DO.open_orgs_mgmt_contract_popup)
                
                })}

            })

       })

    }

})