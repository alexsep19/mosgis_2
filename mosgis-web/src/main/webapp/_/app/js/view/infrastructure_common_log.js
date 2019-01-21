define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'infrastructure_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 17, caption: 'Значения полей'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},

                {field: 'name', caption: 'Наименование', size: 10},
                {field: 'manageroki_label', caption: 'Правообладатель', size: 15},
                {field: 'code_vc_nsi_39', caption: 'Основание управления', size: 20, voc: data.vc_nsi_39},
                {field: 'indefinitemanagement', caption: 'Бессрочное управление', size: 10, voc: {0: "Нет", 1: "Да"}, hidden: true},
                {field: 'endmanagmentdate', caption: 'Окончание управления', size: 18, render: _dt},
                {field: 'code_vc_nsi_33', caption: 'Вид объекта', size: 20, voc: data.vc_nsi_33},
                {field: 'independentsource', caption: 'Автономный источник снабжения', size: 10, voc: {0: "Нет", 1: "Да"}, hidden: true},
                {field: 'code_vc_nsi_34', caption: 'Вид водозаборного сооружения', size: 20, voc: data.vc_nsi_34, hidden: true},
                {field: 'code_vc_nsi_35', caption: 'Тип газораспределительной сети', size: 20, voc: data.vc_nsi_35, hidden: true},
                {field: 'code_vc_nsi_40', caption: 'Вид топлива', size: 20, voc: data.vc_nsi_40, hidden: true},
                {field: 'code_vc_nsi_37', caption: 'Тип электрической подстанции', size: 20, voc: data.vc_nsi_37, hidden: true},
                {field: 'code_vc_nsi_38', caption: 'Вид электростанции', size: 20, voc: data.vc_nsi_38, hidden: true},
                {field: 'oktmo_code', caption: 'ОКТМО', size: 20, hidden: true},
                {field: 'adress', caption: 'Адрес объекта', size: 20, hidden: true},
                {field: 'comissioningyear', caption: 'Год ввода в эксплуатацию', size: 10, hidden: true},
                {field: 'countaccidents', caption: 'Число аварий на 100 км сетей', size: 10, hidden: true},
                {field: 'deterioration', caption: 'Уровень износа (%)', size: 10, hidden: true},
            ],
            
            url: '/mosgis/_rest/?type=infrastructures&part=log&id=' + $_REQUEST.id,        

        }).refresh ();

    }

})