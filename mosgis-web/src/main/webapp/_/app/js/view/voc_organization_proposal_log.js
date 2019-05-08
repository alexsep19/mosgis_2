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

        var is_branch = data.item.id_type == 2
        var is_alien  = data.item.id_type == 3

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
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время', size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор', size: 30},

                {field: 'fullname', caption: 'Полное наименование', size: 100},
                {field: 'ogrn', caption: 'ОГРН', size: 15},
                {field: 'inn', caption: 'ИНН', size: 15},
                {field: 'kpp', caption: 'КПП', size: 10},

                {field: 'nza', caption: 'Номер записи об аккредитации', size: 11, off: !is_alien},
                {field: 'stateregistrationdate', caption: 'Дата внесения в реестр аккредитованных', size: 18, render: _dt, off: !is_alien},
                {field: 'accreditationenddate', caption: 'Дата прекращения аккредитации', size: 18, render: _dt, off: !is_alien},

                {field: 'okopf', caption: 'ОКОПФ', size: 10, off: !is_branch},
                {field: 'stateregistrationdate', caption: 'Дата государственной регистрации', size: 18, render: _dt, off: !is_branch},
                {field: 'activityenddate', caption: 'Дата прекращения деятельности', size: 18, render: _dt, off: !is_branch},

                {field: 'address', caption: 'Адрес', size: 10},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
                
            ].filter(not_off),

            url: '/_back/?type=voc_organization_proposals&part=log&id=' + $_REQUEST.id,

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get(e.recid)

                if ($_USER.role.admin) switch (c.field) {
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