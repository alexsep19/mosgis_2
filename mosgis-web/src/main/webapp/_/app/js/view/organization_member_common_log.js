define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['organization_member_common_layout'].el ('main')).w2regrid ({

            name: 'organization_member_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 9, caption: 'Значения полей'},
            ],

            columns: [

                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 20, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'participant', caption: 'Участие в совете правления, ревизионной комиссии', size: 50, voc: data.vc_org_prtcps},
                {field: 'dt_from', caption: 'Дата принятия', size: 18, render: _dt},
                {field: 'dt_to', caption: 'Дата исключения', size: 18, render: _dt},
                {field: 'phone', caption: 'Телефон', size: 12},
                {field: 'fax', caption: 'Факс', size: 18},
                {field: 'mail',  caption: 'Адрес электронной почты', size: 20},
                {field: 'is_chairman', caption: 'Избран председателем правления', size: 30
                    , render: function (r, ind, col_ind, data) {
                        return r.is_chairman ? 'Да' : ''
                    }
                },
                {field: 'dt_from_chairman', caption: 'Период правления с', size: 25, render: _dt},
                {field: 'dt_to_chairman', caption: 'Период правления по', size: 25, render: _dt},
            ],

            url: '/mosgis/_rest/?type=organization_members&part=log&id=' + $_REQUEST.id,

        }).refresh ();

    }

})