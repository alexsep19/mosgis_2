define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                    {name: 'mainmunicipalservicename', type: 'text'},
                    {name: 'sortorder', type: 'text'},
                    {name: 'okei', type: 'list', options: {items: data.vc_okei.items}},
                    {name: 'code_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2.items}},
                    {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'is_general', type: 'list', options: {items: [{id:0, text:"нет"},{id:1, text:"общедомовые нужды"}]}},
                    {name: 'selfproduced', type: 'list', options: {items: [{id:0, text:"нет"},{id:1, text:"самостоятельное производство"}]}},
                ],

            })

       })

    }

})