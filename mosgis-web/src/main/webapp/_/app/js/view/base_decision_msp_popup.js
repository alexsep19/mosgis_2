define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            data.voc_bool = [
                {id: "0", text: "Нет"},
                {id: "1", text: "Да"}
            ]

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'vc_nsi_302_form',

                record: data.record,

                fields : [                                
                    {name: 'decisionname', type: 'text'},
                    {name: 'code_vc_nsi_301', type: 'list', options: {items: data.vc_nsi_301.items}},
                    {name: 'isappliedtosubsidiaries', type: 'list', options: {items: data.voc_bool}},
                    {name: 'isappliedtorefundofcharges', type: 'list', options: {items: data.voc_bool}},
                ],

            })

       })

    }

})