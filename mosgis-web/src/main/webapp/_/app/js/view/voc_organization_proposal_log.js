define([], function () {

    function _ddt(record, ind, col_ind, data) {
        return data < 99 ? data : 'посл.'
    }

    var nxt = {
        0: 'тек.',
        1: 'след.',
    }

    return function (data, view) {

        data = $('body').data('data')

        $(w2ui ['topmost_layout'].el('main')).w2regrid({

            name: 'voc_organization_proposal_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups: [
                {span: 2, caption: 'Обновление'},
                {span: 8, caption: 'Значения полей'},
                {span: 2, caption: 'Запрос в ГИС ЖКХ'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время', size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор', size: 30},

                {field: 'fullname', caption: 'Полное наименование', size: 100},
                {field: 'ogrn', caption: 'ОГРН', size: 15},
                {field: 'inn', caption: 'ИНН', size: 15},
                {field: 'kpp', caption: 'КПП', size: 10},
                {field: 'okopf', caption: 'ОКОПФ', size: 10},
                {field: 'stateregistrationdate', caption: 'Дата государственной регистрации', size: 18, render: _dt},
                {field: 'activityenddate', caption: 'Дата прекращения деятельности', size: 18, render: _dt},
                {field: 'address', caption: 'Адрес', size: 10},

                {field: 'soap.ts', caption: 'Отправлено', size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано', size: 30, render: _ts, attr: 'data-ref=1'},
            ],

            url: '/mosgis/_rest/?type=voc_organization_proposals&part=log&id=' + $_REQUEST.id,

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get(e.recid)

                switch (c.field) {
                    case 'soap.ts':
                        if (r.uuid_out_soap)
                            return openTab('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp':
                        if (r ['soap.uuid_ack'])
                            return openTab('/out_soap_rp/' + r ['soap.uuid_ack'])
                }

            },

        }).refresh();

        w2ui['voc_organization_proposal_log'].on('columnOnOff:after', function () {
            this.resize()
        })
    }

})