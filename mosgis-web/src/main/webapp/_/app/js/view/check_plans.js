define ([], function () {

    return function (data, view) {

        $(w2ui ['supervision_layout'].el ('main')).w2regrid ({ 

            name: 'check_plans_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                footer: true,
            },

            columns: [
                {field: 'year', caption: 'Год', size: 5},
                {field: 'uriregistrationplannumber', caption: 'Регистрационный номер плана в ЕРП', size: 10},
                {field: 'shouldnotberegistered', caption: 'Не должен быть зарегестрирован в ЕРП', size: 10, render: function (r) {
                    return r.shouldnotberegistered ? 'Да' : 'Нет'
                }},
                {field: 'sign', caption: 'Подписан', size: 10, render: function (r) {
                    return r.sign ? 'Да' : 'Нет'
                }}
            ],

            url: '/mosgis/_rest/?type=check_plans',
            
            onAdd: $_DO.create_check_plans,

            onDblClick: function (e) {
                openTab ('/check_plan/' + e.recid)
            }

        }).refresh ();

    }

})