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
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
            ],
            
            url: '/mosgis/_rest/?type=check_plans&part=log&id=' + $_REQUEST.id

        }).refresh ();

    }

})