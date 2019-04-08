define ([], function () {

    var b = ['delete', 'undelete']

    var is_owner = $_USER.role.nsi_20_2

    var postData = {}
    if (is_owner)
        postData.uuid_org = $_USER.uuid_org
    if ($_USER.has_nsi_20(1, 19, 20, 21, 22))
        postData.uuid_org_customer = $_USER.uuid_org

    var is_popup = 1 == $_SESSION.delete('supply_resource_contracts_popup.on')

    return function (data, view) {

        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el('main')).w2regrid({

            name: 'supply_resource_contracts_grid' + (is_popup ? '_popup' : ''),

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
                items: is_popup? [] : [
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
                        }),
                        off: !is_owner
                    },
                    {
                        type: 'button',
                        id: 'import_objects',
                        caption: 'Импорт договоров ресурсоснабжения...',
                        icon: 'w2ui-icon-plus',
                        onClick: $_DO.import_supply_resource_contracts,
                        off: !$_USER.role.nsi_20_2
                    },
                    {
                        id: 'settlement_docs',
                        type: 'button',
                        text: 'Информация о состоянии расчетов',
                        onClick: function(e) {
                            openTab('/settlement_docs')
                        },
                        off: !($_USER.is_building_society() || $_USER.has_nsi_20(1, 2))
                    }
                ].filter (not_off),
            },

            searches: [
                {field: 'contractnumber', caption: 'Номер', type: 'text'},
                {field: 'signingdate', caption: 'Дата заключения',  type: 'date', operator: 'between'},
                {field: 'completiondate', caption: 'Дата окончания', type: 'date'},
                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 10:
                        case 11:
                        case 12:
                        case 14:
                        case 34:
                        case 40:
                        case 92:
                        case 94:
                        case 100:
                        case 102:
                        case 110:
                        case 104:
                            return true;
                        default:
                            return false;
                    }
                })}},
                {field: 'id_customer_type', caption: 'Тип заказчика', type: 'enum', options: {
                        items: data.vc_gis_sr_customer_type.items
                }},
                {field: 'org_label_uc', caption: 'Исполнитель',  type: 'text', off: is_owner},
                {field: 'customer_label_uc', caption: 'Заказчик', type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}}

            ].filter (not_off),

            onSearch: function(e) {
                e.searchData = e.searchData.map(function(i) { // HACK: between "01.01.2019" AND "" -> 500

                    if (i.type == 'date' && i.operator == 'between' && Array.isArray(i.value)) {

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
                {field: 'id_customer_type', caption: 'Тип заказчика', size: 30, voc: data.vc_gis_sr_customer_type},
                {field: 'org_label', caption: 'Исполнитель', size: 100, off: is_owner},
                {field: 'customer_label', caption: 'Заказчик', size: 100},


            ].filter (not_off),

            postData: {data: postData},

            url: '/_back/?type=supply_resource_contracts',

            onRequest: function (e) {

                if (is_popup) {

                    var post_data = this.post_data || $_SESSION.delete('supply_resource_contracts_popup.post_data');

                    if (post_data) {

                        if (e.postData.search) {
                            $.each(e.postData.search, function () {
                                post_data.search.push(this)
                            })
                        }

                        $.extend(e.postData, post_data)

                        this.post_data = post_data
                    }
                }
            },

            onAdd:      $_DO.create_supply_resource_contracts,

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('supply_resource_contracts_popup.data', clone (r))

                    w2popup.close ()

                } else {
                    openTab ('/supply_resource_contract/' + e.recid)
                }
            },

        }).refresh ();

    }

})