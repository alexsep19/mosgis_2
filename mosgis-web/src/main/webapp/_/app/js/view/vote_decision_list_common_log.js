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
                {span: 1, caption: '', master: true},
                {span: 5, caption: 'Результаты голосования'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 8, render: _ts},
                {field: 'action', caption: 'Действие',    size: 8, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 8},

                {field: 'decisiontype_vc_nsi_63', caption: 'Тип вопроса', size: 15, voc: data.vc_nsi_63},

                {field: 'agree', caption: 'За', size: 4},
                {field: 'against', caption: 'Против', size: 4},
                {field: 'abstent', caption: 'Воздержались', size: 4},
                {field: 'total', caption: 'Всего', size: 4},
                {field: 'votingresume', caption: 'Итоги голосования', size: 4,
                    render: function (r) {return r.votingresume == "M" ? 'Решение принято' : 'Решение не принято'}
                },
            ],
            
            url: '/mosgis/_rest/?type=vote_decision_lists&part=log&id=' + $_REQUEST.id,            

        }).refresh ();

    }

})