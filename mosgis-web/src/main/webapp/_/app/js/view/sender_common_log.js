define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'sender_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 5, caption: 'Значения полей'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'label', caption: 'Сокр. наим.',    size: 20},
                {field: 'label_full', caption: 'Наименование системы',    size: 50},
                {field: 'contact', caption: 'Ответственный за интеграцию',    size: 50},
                {field: 'login', caption: 'login',    size: 20},
                {field: 'is_locked', caption: 'Интеграция отключена',  size: 10, voc: {1: 'отключена', 0: 'включена'}},
                
            ],
            
            url: '/_back/?type=senders&part=log&id=' + $_REQUEST.id,

        }).refresh ();

    }

})