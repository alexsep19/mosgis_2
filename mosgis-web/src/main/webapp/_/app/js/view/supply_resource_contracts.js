define ([], function () {

    var b = ['delete', 'undelete']

    var is_owner = $_USER.role.nsi_20_2

    var postData = {}
    if (is_owner) postData.uuid_org = $_USER.uuid_org

    return function (data, view) {

        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({

            name: 'supply_resource_contracts_grid',

            show: {
                toolbarSearch: true,
                toolbar: true,
                footer: true,
            },

            toolbar: {
                onClick: function (e) {
                    if (/^create/.test(e.item.id) && e.subItem) {
                        $_SESSION.set('record', {id_customer_type: e.subItem.id})
                        use.block('supply_resource_contract_new')
                    }
                },
                items: !is_owner ? [] : [
                    {
                        id: 'create',
                        type: 'menu',
                        text: 'Добавить',
                        selected: -1,
                        items: data.vc_gis_sr_customer_type.items.map(function (i) {
                            return {
                                id: i.id,
                                text: (i.id == 6? '' : 'Заказчик: ') + i.text
                            }
                        })
                    }
                ].filter (not_off),
            },

            searches: [
                {field: 'contractnumber', caption: 'Номер', type: 'text'},
                {field: 'signingdate', caption: 'Дата заключения',  type: 'date', operator: 'between'},
                {field: 'completiondate', caption: 'Дата окончания', type: 'date'},
                {field: 'id_ctr_state_gis', caption: 'Состояние договора', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter(function (i) {
                    switch (i.id) {
                        case 50:
                        case 60:
                        case 80:
                            return false;
                        default:
                            return true;
                    }
                })}},
                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 50:
                        case 60:
                        case 80:
                            return false;
                        default:
                            return true;
                    }
                })}},
//                {field: '???', caption: 'Вид коммунальной услуги', size: 10, voc: data.???},
//                {field: '???', caption: 'Вид коммунального ресурса', size: 10, voc: data.???},
//                {field: '???', caption: 'Вид договора', size: 10, voc: data.???},
                {field: 'is_customer_org', caption: 'Тип заказчика', type: 'list', options: {items: [
                    {id: "0", text: "Физическое лицо"},
                    {id: "1", text: "Юридическое лицо"},
                ]}},
                {field: 'customer_label_uc', caption: 'Заказчик',  type: 'text'},
                {field: 'org_label_uc', caption: 'Исполнитель',  type: 'text', off: is_owner},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}}

            ].filter (not_off),

            onSearch: function(e) {
                e.searchData = e.searchData.map(function(i) { // HACK: between "01.01.2019" AND "" -> 500

                    if (i.type == 'date' && i.operator == 'between') {

                        i.value = i.value.map(function(v, idx) {
                            if (v) return v
                            var y = new Date().getFullYear()
                            return idx? ('31.12.' + y) : ('01.01.' + y)
                        })
                    }
                    return i
                })
            },

            columns: [

                {field: 'contractnumber', caption: 'Номер', size: 20},
                {field: 'signingdate', caption: 'Дата заключения', size: 11, render: _dt},
                {field: 'completiondate', caption: 'Дата окончания', size: 11, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
//                {field: '???', caption: 'Вид коммунальной услуги', size: 10, voc: data.???},
//                {field: '???', caption: 'Вид коммунального ресурса', size: 10, voc: data.???},
//                {field: '???', caption: 'Вид договора', size: 10, voc: data.???},
//                {field: 'address', caption: 'Адрес', size: 100},
                {field: 'is_customer_org', caption: 'Тип заказчика', size: 10, voc: {0: 'физ. лицо', 1: 'юр. лицо'}},
                {field: 'org_label', caption: 'Исполнитель', size: 100, off: is_owner},
                {field: 'customer_label', caption: 'Заказчик', size: 100},


            ].filter (not_off),

            postData: {data: postData},

            url: '/mosgis/_rest/?type=supply_resource_contracts',

            onAdd:      $_DO.create_supply_resource_contracts,

            onDblClick: function (e) {
                openTab ('/supply_resource_contract/' + e.recid)
            },

        }).refresh ();

    }

})