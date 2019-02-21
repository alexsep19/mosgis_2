define ([], function () {

    var grid_name = 'supply_resource_contract_object_subjects_grid'

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
                {field: 'code_vc_nsi_239', caption: 'Коммунальный ресурс', type: 'enum', options: {items: data.vc_nsi_239.items}},
            ],

            textSearch: 'contains',

            columns: [
                {field: 'code_vc_nsi_3', caption: 'Коммунальная услуга', size: 40, voc: data.vc_nsi_3},
                {field: 'code_vc_nsi_239', caption: 'Коммунальный ресурс', size: 40, voc: data.vc_nsi_239},
                {field: 'startsupplydate', caption: 'Дата начала поставки', size: 20, render: _dt},
                {field: 'endsupplydate', caption: 'Дата окончания поставки', size: 20, render: _dt},
            ],

            postData: {data: {
                uuid_sr_ctr_obj: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=supply_resource_contract_object_subjects',

            onDblClick: function (e) {
                openTab ('/supply_resource_contract_object_subject/' + e.recid)
            },

            onAdd: $_DO.create_supply_resource_contract_object_subjects,

            onDelete: $_DO.delete_supply_resource_contract_object_subjects
        })

    }

})