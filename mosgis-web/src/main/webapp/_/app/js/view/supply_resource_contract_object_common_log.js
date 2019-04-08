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

            name: 'supply_resource_contract_object_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 3, caption: 'Значения полей'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},

                {field: 'fiashouseguid', caption: 'Адрес', size: 40},
                {field: 'premise.label', caption: 'Помещение', size: 20},
                {field: 'is_deleted',  caption: 'Статус',     size: 20, voc: {0: 'Актуально', 1: 'Удалено'}},
            ],

            url: '/_back/?type=supply_resource_contract_objects&part=log&id=' + $_REQUEST.id

        }).refresh ();

    }

})