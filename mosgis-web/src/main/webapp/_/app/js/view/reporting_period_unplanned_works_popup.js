define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'reporting_period_unplanned_works_popup_form',

                record: data.record,

                fields : [                                
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

            })

       })

    }

})