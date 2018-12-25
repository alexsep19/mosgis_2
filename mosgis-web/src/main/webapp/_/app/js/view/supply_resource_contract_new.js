define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            var f = 'supply_resource_contract_new_form'

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'id_customer_type', type: 'list', options: {items: data.vc_gis_sr_customer_type.items}},
                    {name: 'id_c_type', type: 'radio', options: {items: data.voc_c_types}},
                    {name: 'contractnumber', type: 'text'},
                    {name: 'uuid_person_customer', type: 'list', options: {items: data.persons}},
                    {name: 'uuid_org_customer', type: 'text', hidden: true},
                    {name: 'label_org_customer', type: 'text'},

                    {name: 'signingdate', type: 'date'},
                    {name: 'code_vc_nsi_58', type: 'list', options: {items: data.vc_nsi_58.items}},
                    {name: 'effectivedate', type: 'date'},
                    {name: 'completiondate', type: 'date'},
                ],

                focus: -1,

                onRefresh: function(e) {
                    e.done(function(){
                        var toggle_customer = function (value) {
                            var empty = !(value > 0)
                            $('#field_org_customer').toggle(value == 1)
                            $('#field_person_customer').toggle(value == 2 || empty)
                            $('#field_person_customer input').prop('readonly', empty)
                            if (value == 1 || empty) {
                                delete w2ui[f].record['uuid_person_customer']
                            }
                            if (value == 2 || empty) {
                                delete w2ui[f].record['uuid_org_customer']
                                delete w2ui[f].record['label_org_customer']
                            }
                        }
                        $('input[name=id_customer_type]').prop('readonly', true)
                        var r = w2ui[f].record
                        var value = r.id_c_type? r.id_c_type : 0
                        toggle_customer(value)

                        clickOn($('span.radio input'), function(){ toggle_customer(this.value) })
                        clickOn($('span.radio span.radio-item'), function (e) {
                            $(this).prev('input').click()
                        })
                        clickOn($('#label_org_customer'), $_DO.open_orgs_supply_resource_contract_org_new)
                    })
                }

            }).refresh()

       })

    }

})