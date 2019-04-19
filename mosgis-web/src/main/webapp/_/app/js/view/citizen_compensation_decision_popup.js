define ([], function () {

    var form_name = 'citizen_compensation_decision_popup_form'

    function recalc () {
        var data = clone ($('body').data ('data'))
        var f = w2ui [form_name]

        if (f.record.code_vc_nsi_301) {
            f.get('code_vc_nsi_302').options.items = dia2w2uiRecords (
                data.vc_nsi_302.items.filter (x => x.code_vc_nsi_301 == f.record.code_vc_nsi_301.id)
                || []
            )
            delete f.record.code_vc_nsi_302

            f.refresh ()
        }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'number_', type: 'text'},
                    {name: 'decisiondate', type: 'date'},
                    {name: 'code_vc_nsi_301', type: 'list', options: {items: data.vc_nsi_301.items}},
                    {name: 'code_vc_nsi_302', type: 'list', options: {items: []}},
                    {name: 'eventdate', type: 'date'},
                ],

                onRender: function (e) { e.done (setTimeout (recalc, 100)) },

                onChange: function (e) {

                    if (e.target == 'code_vc_nsi_301') e.done (function () {
                        recalc () 
                        this.refresh ()
                    })

                },

                focus: data.record.id? -1 : 0
            })
       })

    }

})