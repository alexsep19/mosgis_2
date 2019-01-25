define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'infrastructure_resources_popup_form',

                record: data.record,

                fields : [
                    {name: 'code_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2_filtered.items}},
                    {name: 'setpower', type: 'text'},
                    {name: 'sitingpower', type: 'text'},
                    {name: 'totalload', type: 'text'},
                    {name: 'industrialload', type: 'text'},
                    {name: 'socialload', type: 'text'},
                    {name: 'populationload', type: 'text'}
                ]

            })

       })

    }

})