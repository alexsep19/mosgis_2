define ([], function () {

    var grid_name = 'supply_resource_contract_objects_grid'

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
                toolbarSearch: false,
                toolbarAdd: is_editable
            },

            searches: [
            ],

            textSearch: 'contains',

            columns: [
                {field: 'building.label', caption: 'Адрес', size: 40, render: function(i) {
                    if (!i.uuid_premise) {
                        return i['building.label']
                    }

                    return i['building.label'] + ', ' + i['premise.label']
                }},
                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status}
            ],

            postData: {search: [
                {field: "uuid_sr_ctr", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=supply_resource_contract_objects',

            onDblClick: function (e) { openTab ('/supply_resource_contract_object/' + e.recid) },

            onAdd: $_DO.create_supply_resource_contract_objects
        })

    }

})