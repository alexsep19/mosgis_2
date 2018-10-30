define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'vc_person_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 12, caption: 'Значения полей'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'surname', caption: 'Фамилия', size: 30},
                {field: 'firstname', caption: 'Имя', size: 30},
                {field: 'patronymic', caption: 'Отчество', size: 30},
                {field: 'is_female', caption: 'Пол', size: 20, voc: {0: "Мужской", 1: "Женский"}},
                {field: 'placebirth', caption: 'Место рождения', size: 20, hidden: 1},
                {field: 'birthdate', caption: 'Дата рождения', size: 18, render: _dt},
                {field: 'snils', caption: 'СНИЛС', size: 20, hidden: 1},
                {field: 'code_vc_nsi_95',  caption: 'Код документа',     size: 20, voc: data.vc_nsi_95},
                {field: 'series', caption: 'Серия документа', size: 20, hidden: 1},
                {field: 'number_', caption: 'Номер документа', size: 20, hidden: 1},
                {field: 'issuedate', caption: 'Дата выдачи документа', size: 18, render: _dt, hidden: 1},
                {field: 'issuer', caption: 'Кем выдан документ', size: 20, hidden: 1},

            ],
            
            url: '/mosgis/_rest/?type=vc_persons&part=log&id=' + $_REQUEST.id,            

        }).refresh ();

    }

})