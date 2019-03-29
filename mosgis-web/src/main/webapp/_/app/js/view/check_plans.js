define ([], function () {

	function perms () {

        return $_USER.role.nsi_20_4 || $_USER.role.nsi_20_5

    }
	
	function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['check_plans_grid']

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).sign

        if (!status) t.enable ('deleteButton')

    })}

    return function (data, view) {

        $(w2ui ['supervision_layout'].el ('main')).w2regrid ({

            name: 'check_plans_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'createButton', caption: 'Добавить', onClick: $_DO.create_check_plans, icon: 'w2ui-icon-plus', off: !perms()},
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_check_plans, icon: 'w2ui-icon-cross', disabled: true},
                    {type: 'button', id: 'importButton', caption: 'Импорт из ГИС ЖКХ', onClick: $_DO.import_check_plans, icon: 'w2ui-icon-plus'},
                ].filter (not_off)
            },

            searches: [
                {field: 'uriregistrationplannumber', caption: 'Регистрационный номер', type: 'int', operator: 'is', operators: ['is']},
            ],

            columns: [
                {field: 'year', caption: 'Год', size: 5},
                {field: 'uriregistrationplannumber', caption: 'Регистрационный номер плана в ЕРП', size: 10},
                {field: 'shouldberegistered', caption: 'Должен быть зарегистрирован в ЕРП', size: 10, render: function (r) {
                    return r.shouldberegistered ? 'Да' : 'Нет'
                }},
                {field: 'sign', caption: 'Подписан', size: 10, render: function (r) {
                    return r.sign ? 'Да' : 'Нет'
                }}
            ],

            url: '/mosgis/_rest/?type=check_plans',

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onDblClick: function (e) {
                openTab ('/check_plan/' + e.recid)
            }

        }).refresh ();

    }

})