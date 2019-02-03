define ([], function () {

    var form_name = 'infrastructure_transportation_resources_popup_form'

    function reset_okei () {

        var data = clone ($('body').data ('data'))
        var record = w2ui[form_name].record

        if (record.code_vc_nsi_2) {
            okei_label = data.vc_okei[data.vc_nsi_2_filtered.items.find (x => x.id == record.code_vc_nsi_2.id).okei]
            $('.span_okei').text (' ' + okei_label)
        }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'code_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2_filtered.items}},
                    {name: 'volumelosses', type: 'text'},
                    {name: 'totalload', type: 'text'},
                    {name: 'industrialload', type: 'text'},
                    {name: 'socialload', type: 'text'},
                    {name: 'populationload', type: 'text'},
                    {name: 'code_vc_nsi_41', type: 'list', options: {items: data.vc_nsi_41.items}},
                ],

                onRefresh: function (e) {

                    e.done (reset_okei)

                },

                onChange: function (e) {

                    if (e.target == 'code_vc_nsi_2') {
                        e.done (reset_okei)
                    }

                }

            })

       })

    }

})