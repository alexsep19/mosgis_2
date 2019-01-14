define ([], function () {

    function recalc () {

        var shouldberegistered = clone ($('body').data ('data')).item.shouldberegistered
        var $reg_num = $('#uriregistrationnumber')

        if (!shouldberegistered) { 
            $reg_num.prop ('disabled', true)
            $reg_num.prop ('placeholder', 'План не должен быть зарегистрирован в ЕРП')
        }
        else { $reg_num.prop ('disabled', false) }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'planned_examination_grid',

                record: data.record,

                fields : [
                    {name: 'numberinplan', type: 'text'},
                    {name: 'uriregistrationnumber', type: 'text'},
                    {name: 'uriregistrationdate', type: 'date'},
                    {name: 'code_vc_nsi_65', type: 'list', options: {items: data.vocs.vc_nsi_65.items}},
                    {name: 'code_vc_nsi_71', type: 'list', options: {items: data.vocs.vc_nsi_71.items}},
                ],

                onRender: function (e) { e.done (setTimeout (recalc, 100)) }

            })

       })

    }

})