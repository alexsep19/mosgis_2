define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'vc_oh_wk_types_popup_form',

                record: data.record,

                fields : [
                    {name: 'servicename', type: 'text'},
                    {name: 'code_vc_nsi_218', type: 'list', options: {items: data.vc_nsi_218.items}},
                ],

            })

       })

    }

})