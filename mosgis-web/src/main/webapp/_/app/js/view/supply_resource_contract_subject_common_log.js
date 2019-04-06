define ([], function () {

    function _ddt (record, ind, col_ind, data) {
        return data < 99 ? data : 'посл.'
    }

    var nxt = {
        0: 'тек.',
        1: 'след.',
    }

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            name: 'supply_resource_contract_subject_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 8, caption: 'Значения полей'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор/поставщик', size: 30, render: function (r) {return r ['tb_senders.label'] || r ['vc_users.label']}},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},

                {field: 'code_vc_nsi_3', caption: 'Вид коммунальной услуги', size: 40, voc: data.vc_nsi_3},
                {field: 'code_vc_nsi_239', caption: 'Коммунальный ресурс', size: 40, voc: data.vc_nsi_239},
                {field: 'startsupplydate', caption: 'Поставка ресурса с', size: 22, render: _dt},
                {field: 'endsupplydate', caption: 'Поставка ресурса по', size: 22, render: _dt},
                {field: 'volume', caption: 'Плановый объем', size: 30},
                {field: 'unit', caption: 'Ед. изм.', size: 15, voc: data.vc_okei},
                {field: 'feedingmode', caption: 'Режим подачи', size: 50, hidden: true},
                {field: 'is_deleted',  caption: 'Статус',     size: 20, voc: {0: 'Актуально', 1: 'Удалено'}},
            ],

            url: '/_back/?type=supply_resource_contract_subjects&part=log&id=' + $_REQUEST.id

        }).refresh ();

    }

})