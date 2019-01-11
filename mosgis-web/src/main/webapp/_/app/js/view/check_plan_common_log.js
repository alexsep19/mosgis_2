define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'check_plan_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },
            
            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 4, caption: 'Значения полей'},
            ], 

            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 10, render: _ts},
                {field: 'action', caption: 'Действие',    size: 10, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 10},

                {field: 'year', caption: 'Год плана', size: 5},
                {field: 'sign', caption: 'Подписан', size: 5, render: function (r) {
                    return r.sign ? 'Да' : 'Нет'
                }},
                {field: 'shouldberegistered', caption: 'Должен быть зарегистрирован в ЕРП', size: 10, render: function (r) {
                    return r.shouldberegistered ? 'Да' : 'Нет'
                }},
                {field: 'uriregistrationplannumber', caption: 'Регистрационный номер плана в ЕРП', size: 10},
            ],
            
            url: '/mosgis/_rest/?type=check_plans&part=log&id=' + $_REQUEST.id

        }).refresh ();

    }

})