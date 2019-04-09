define ([], function () {

    var grid_name = 'payments_grid'

    return function (data, view) {

        var layout = w2ui ['service_payments_layout']

        var $panel = $(layout.el ('main'))

        var it = data.item

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarAdd: data._can.create,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: true,
            },

            textSearch: 'contains',

            searches: [
//                {field: 'dt_period', caption: 'Период',         type: 'date', operator: 'between', operators: ['between']},
//                {field: 'id_ctr_status', caption: 'Статус',     type: 'enum', options: {items: data.vc_gis_status.items}},
//                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
//                    {id: "0", text: "Актуальные"},
//                    {id: "1", text: "Удалённые"},
//                ]}},
            ].filter (not_off),

            columns: [

                {field: 'ordernum', caption: 'Номер', size: 20},

                {field: 'dt_period', caption: 'Период', size: 22, render: function (r) {
                    return w2utils.settings.fullmonths [r.month - 1] + ' ' + r.year
                }},

                {field: 'amount', caption: 'Сумма, руб.', size: 10, render: 'float:2'},

                {field: 'org.label', caption: 'Плательщик', size: 50},

                {field: 'paymentpurpose', caption: 'Назначение', size: 50},

                {field: 'pd.paymentdocumentnumber', caption: 'Квитаниця', size: 20},

                {field: 'acct.accountnumber', caption: '№ лицевого счета', size: 20},

                {field: 'id_ctr_status', caption: 'Статус', size: 15, voc: data.vc_gis_status},

            ],

            postData: {data: {uuid_org: $_USER.uuid_org}},

            url: '/_back/?type=payments',

            onDblClick: function (e) {openTab ('/payment/' + e.recid)}

        })

    }

})