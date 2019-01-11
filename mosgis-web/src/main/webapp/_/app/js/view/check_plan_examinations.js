define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['check_plans_grid']

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).sign

        if (!status) t.enable ('deleteButton')

    })}

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'check_plan_examinations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_check_plan_examinations, icon: 'w2ui-icon-cross', disabled: true},
                ]
            },

            columns: [
                {field: 'numberinplan', caption: 'Номер', size: 5},
                {field: 'code_vc_nsi_71', caption: 'Форма проведения проверки', size: 10},
                {field: 'shouldberegistered', caption: 'Должен быть зарегистрирован в ЕРП', size: 10, render: function (r) {
                    return r.shouldnotberegistered ? 'Да' : 'Нет'
                }},
                {field: 'sign', caption: 'Подписан', size: 10, render: function (r) {
                    return r.sign ? 'Да' : 'Нет'
                }}
            ],

            url: '/mosgis/_rest/?type=check_plans',
            
            onAdd: $_DO.create_check_plans,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onDblClick: function (e) {
                openTab ('/check_plan/' + e.recid)
            }

        }).refresh ();

    }

})