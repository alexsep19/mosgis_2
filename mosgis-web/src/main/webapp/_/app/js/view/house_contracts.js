define ([], function () {

    var grid_name = 'house_contracts_grid'

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
            },            

            textSearch: 'contains',

            columns: [                
                {field: 'type', caption: 'Тип', size: 15, voc: {
                    mgmt_contract: 'Договор управления',
                    charter: 'Устав',
                    supply_resource_contract: 'Договор ресурсноснабжения',
                    public_property_contract: 'Договор пользования общим имуществом',
                    rc_contract: 'Договор услуг РЦ',
                }},
                {field: 'no', caption: 'Номер', size: 10},
                {field: 'dt', caption: 'Дата', size: 18, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'org_label', caption: 'Исполнитель', size: 30},
                {field: 'customer_label', caption: 'Заказчик', size: 30},
                {field: 'reason', caption: 'Основание заключения', size: 10},
                {field: 'dt_from', caption: 'Дата начала', size: 18, render: _dt},
                {field: 'dt_to', caption: 'Дата окончания', size: 18, render: _dt},
                {field: 'id_obj_status', caption: 'Статус дома', size: 20, voc: data.vc_gis_status},
            ],            

            records: data.lines,

            onDblClick: function (e) {openTab ('/' + e.recid)},

        }).refresh ()

    }

})