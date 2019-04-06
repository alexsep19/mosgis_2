define ([], function () {

    var grid_name = 'supply_resource_contract_temperature_charts_grid'

    return function (data, view) {

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit && data.is_on_tab_temperature

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
                {field: 'outsidetemperature', caption: 'Температура наружного воздуха, °С', size: 40},
                {field: 'flowlinetemperature', caption: 'Температура теплоносителя в подающем трубопроводе, °С', size: 40},
                {field: 'oppositelinetemperature', caption: 'Температура теплоносителя в обратном трубопроводе, °С', size: 40},
            ],

            postData: {data: {
                uuid_sr_ctr: $_REQUEST.id
            }},

            url: '/_back/?type=supply_resource_contract_temperature_charts',

            onDblClick: !is_editable ? null : function (e) {

                var grid = w2ui [e.target]

                var r = grid.get(e.recid)

                r.uuid_sr_ctr = $_REQUEST.id

                $_SESSION.set('record', r)

                use.block ('supply_resource_contract_temperature_charts_popup')
            },

            onAdd: $_DO.create_supply_resource_contract_temperature_charts,

            onDelete: $_DO.delete_supply_resource_contract_temperature_charts
        })

    }

})