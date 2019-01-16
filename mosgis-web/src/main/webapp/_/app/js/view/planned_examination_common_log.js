define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            name: 'planned_examination_common_log',

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

                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 20, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'numberinplan', caption: 'Номер', size: 10},
                {field: 'code_vc_nsi_71', caption: 'Форма проведения проверки', size: 30, voc: data.vc_nsi_71},
                {field: 'subject_label', caption: 'Субъект проверки', size: 30},
                {field: 'objective', caption: 'Цель проведения проверки', size: 50},
            ],

            url: '/mosgis/_rest/?type=planned_examinations&part=log&id=' + $_REQUEST.id,

        }).refresh ();

    }

})