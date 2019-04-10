define ([], function () {

    var grid_name = 'account_payments_grid'

    return function (data, view) {

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        var it = data.item

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: 1,
                footer: 1,
                toolbarAdd: it._can.create_payments,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: true,
            },
/*
            toolbar: {

                items: [
                    {type: 'button', id: 'ind', caption: 'Добавить ЛС физического лица', onClick: $_DO.create_account_payments, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                    {type: 'button', id: 'org', caption: 'Добавить ЛС юридического лица', onClick: $_DO.create_account_payments, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                ].filter (not_off),

            },
*/
            textSearch: 'contains',

            searches: [
                {field: 'dt_period', caption: 'Период',         type: 'date', operator: 'between', operators: ['between']},
                {field: 'id_ctr_status', caption: 'Статус',     type: 'enum', options: {items: data.vc_gis_status.items}},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
//                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),

            columns: [

                {field: 'ordernum', caption: 'Номер', size: 10},

                {field: 'orderdate', caption: 'Дата', size: 10, render: _dt},

                {field: 'dt_period', caption: 'Период', size: 12, render: function (r) {
                    return w2utils.settings.fullmonths [r.month - 1] + ' ' + r.year
                }},

                {field: 'amount', caption: 'Сумма, руб.', size: 15, render: 'float:2'},

                {field: 'org_customer.label', caption: 'Плательщик', size: 50, render: function(i){
                        return i['org_customer.label'] || i['ind_customer.label']
                }},

                {field: 'paymentpurpose', caption: 'Назначение', size: 50},

                {field: 'pd.paymentdocumentnumber', caption: 'Квитаниця', size: 20},

                {field: 'acct.accountnumber', caption: '№ лицевого счета', size: 20},

                {field: 'acct.serviceid', caption: 'Идентификатор поставщика услуг', size: 30},

                {field: 'org.label', caption: 'Организация, принявшая платеж', size: 30},

                {field: 'id_ctr_status', caption: 'Статус', size: 15, voc: data.vc_gis_status},

            ],

            postData: {data: {uuid_account: $_REQUEST.id}},

            url: '/_back/?type=payments',

            onDblClick: function (e) {openTab ('/payment/' + e.recid)},

            onAdd: $_DO.create_account_payments,

        })

    }

})