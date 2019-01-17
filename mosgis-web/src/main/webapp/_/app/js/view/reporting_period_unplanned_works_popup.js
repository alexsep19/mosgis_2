define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'reporting_period_unplanned_works_popup_form',

                record: data.record,

                fields : [                                                
                    {name: 'code_vc_nsi_56', type: 'hidden'},
                    {name: 'accidentreason', type: 'text'},
                    {name: 'amount', type: 'text'},
                    {name: 'code_vc_nsi_3', type: 'text', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_57', type: 'text', type: 'list', options: {items: data.vc_nsi_57.items}},
                    {name: 'comment_', type: 'text'},
                    {name: 'count', type: 'text'},
                    {name: 'organizationguid', type: 'text'},
                    {name: 'price', type: 'text'},
                    {name: 'uuid_org_work', type: 'list', options: {items: data.org_works.items}},
                ],
                
                onChange: function (e) {               

                    if (e.target == "uuid_org_work") {
                        $('#unit').text (e.value_new.unit)
                        this.record.code_vc_nsi_56 = e.value_new.code_vc_nsi_56
                    }

                }

            })

       })

    }

})