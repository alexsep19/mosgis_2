define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'vote_decision_list_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 7, caption: 'Значения полей'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 10, render: _ts},
                {field: 'action', caption: 'Действие',    size: 10, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 10},
            ],
            
            url: '/mosgis/_rest/?type=vote_decision_lists&part=log&id=' + $_REQUEST.id,            

        }).refresh ();

    }

})