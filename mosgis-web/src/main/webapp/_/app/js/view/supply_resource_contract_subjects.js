define ([], function () {

    var grid_name = 'supply_resource_contract_subjects_grid'

    return function (data, view) {

        var layout = w2ui ['topmost_layout']

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
                toolbarAdd: is_editable
            },

            searches: [
                {field: 'code_vc_nsi_3', caption: 'Вид коммунальной услуги', type: 'enum', options: {items: data.vc_nsi_3.items}},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ],

            textSearch: 'contains',

            columns: [
                {field: 'code_vc_nsi_3', caption: 'Вид коммунальной услуги', size: 40, voc: data.vc_nsi_3},
                {field: 'code_vc_nsi_239', caption: 'Коммунальный ресурс', size: 40, voc: data.vc_nsi_239},
                {field: 'startsupplydate', caption: 'Дата начала поставки ресурса', size: 40, render: _dt},
                {field: 'endsupplydate', caption: 'Дата окончания поставки ресурса', size: 40, render: _dt},
                {field: 'volume', caption: 'Плановый объем', size: 30},
                {field: 'unit', caption: 'Ед. изм.', size: 20, voc: data.vc_okei},
                {field: 'feedingmode', caption: 'Режим подачи', size: 100},
            ],

            postData: {data: {
                uuid_sr_ctr: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=supply_resource_contract_subjects',

            onDblClick: function (e) { openTab ('/supply_resource_contract_subject/' + e.recid) },

            onAdd: $_DO.create_supply_resource_contract_subjects
        })

    }

})