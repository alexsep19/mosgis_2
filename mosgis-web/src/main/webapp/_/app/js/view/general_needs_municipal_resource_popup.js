define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                    {name: 'sortorder', type: 'text'},
                    {name: 'generalmunicipalresourcename', type: 'text'},
                    {name: 'okei', type: 'list', options: {items: data.vc_okei.items}},
                    {name: 'parentcode', type: 'list', options: {items: data.parents.items}},
                    {name: 'code_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2.items}},
                ],

            })

       })

    }

})