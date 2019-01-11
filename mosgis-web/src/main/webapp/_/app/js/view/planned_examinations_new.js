define ([], function () {

    //function recalc () {

    //    var $reg_num = $('#uriregistrationplannumber')
    //    var v = w2ui ['check_plan_form'].values ()

    //    if (!v.shouldberegistered) { $reg_num.prop ('disabled', true) }
    //    else { $reg_num.prop ('disabled', false) }

    //}

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'planned_examination_grid',

                record: data.record,

                fields : [
                    {name: 'numberinplan', type: 'text'},
                    {name: 'uriregistrationnumber', type: 'text'},
                    {name: 'uriregistrationdate', type: 'date'},
                    {name: 'code_vc_nsi_65', type: 'list', voc: data.vc_nsi_65},
                    {name: 'code_vc_nsi_71', type: 'list', voc: data.vc_nsi_71},
                ],

                //onChange: function (e) { if (e.target == 'shouldberegistered') { e.done (recalc) } },
                //onRender: function (e) { e.done (setTimeout (recalc, 100)) }

            })

       })

    }

})