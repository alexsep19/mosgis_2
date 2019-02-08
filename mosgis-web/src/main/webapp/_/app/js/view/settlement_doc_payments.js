define ([], function () {

    var grid_name = 'settlement_doc_payments_grid'

    return function (data, view) {

        var layout = w2ui ['passport_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbar: true,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarReload: false,
                toolbarAdd: is_editable,
                toolbarDelete: is_editable
            },

            searches: [
            ],

            textSearch: 'contains',

            columns: [
                {field: 'period', caption: 'Отчетный период', size: 30, render: function(i){
                    return w2utils.settings.fullmonths [i.month - 1].toLowerCase()
                        + ' ' + i.year
                }},
                {field: 'credited', caption: 'Начислено, руб.', size: 40, render: 'money:2'},
                {field: 'receipt', caption: 'Поступило, руб.', size: 40, render: 'money:2'},
                {field: 'paid', caption: 'Оплачено, руб.', size: 40, render: 'money:2'},
                {field: 'debts', caption: 'Задолженность, руб.', size: 40, render: 'money:2'},
                {field: 'overpayment', caption: 'Переплата, руб.', size: 40, render: 'money:2'},
                {field: 'id_sp_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'annulmentreason', caption: 'Причина аннулирования', size: 40},
                {field: 'version', caption: 'Версия', size: 20},
            ],

            postData: {data: {
                uuid_st_doc: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=settlement_doc_payments',

            onDblClick: !is_editable ? null : function (e) {

                var grid = w2ui [e.target]

                var r = grid.get(e.recid)

                r.uuid_sr_ctr = $_REQUEST.id

                $_SESSION.set('record', r)

                use.block ('settlement_doc_payments_popup')
            },

            onAdd: $_DO.create_settlement_doc_payments,

            onDelete: $_DO.delete_settlement_doc_payments
        })

    }

})