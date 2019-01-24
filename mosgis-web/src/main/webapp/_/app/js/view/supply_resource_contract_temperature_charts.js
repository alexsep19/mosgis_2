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

            url: '/mosgis/_rest/?type=supply_resource_contract_temperature_charts',

            onDblClick: function (e) {

		var grid = w2ui [e.target]

		var r = grid.get(e.recid)

		r.uuid_sr_ctr_obj = $_REQUEST.id

		$_SESSION.set('record', r)

		use.block ('supply_resource_contract_temperature_charts_popup')
	    },

            onAdd: $_DO.create_supply_resource_contract_temperature_charts,

	    onDelete: $_DO.delete_supply_resource_contract_temperature_charts,

	    onRefresh: function(e) {
		e.done(function(){
		    if (!is_editable) {
			$('#tabs_topmost_layout_main_tabs_tab_supply_resource_contract_temperature_charts')
			    .attr('title', 'Доступна при наличии в договоре ресурса "Тепловая энергия"')
		    }
		})
	    }
        })

    }

})