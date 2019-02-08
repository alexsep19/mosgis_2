define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            var f = 'rc_contract_new_form'

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'contractnumber', type: 'text'},
                    {name: 'signingdate', type: 'date'},
                    {name: 'effectivedate', type: 'date'},
                    {name: 'completiondate', type: 'date'},
                    {name: 'uuid_org', type: 'text', hidden: true},
                    {name: 'label_org', type: 'text'},
                    {name: 'id_service_type', type: 'list', options: {items: data.vc_rc_ctr_service_types.items}},
                ],

                focus: 0,

                onRefresh: function(e) {
                    e.done(function(){

                        if ($_USER.has_nsi_20(8)) { // is_oms
                            $('input[name=id_service_type]').prop('disabled', true)
                        }

                        var search = [{
                            field: "code_vc_nsi_20",
                            operator: "in",
                            type: "enum",
                            value: [
                                {
                                    "id": "36",
                                    "text": "РЦ"
                                }
                            ]
                        }]
                        $_SESSION.set('voc_organization_popup.post_data', {search: search, searchLogic: 'AND'})

                        clickOn($('#label_org'), $_DO.open_orgs_rc_contract_new)
                    })
                }

            }).refresh()

       })

    }

})