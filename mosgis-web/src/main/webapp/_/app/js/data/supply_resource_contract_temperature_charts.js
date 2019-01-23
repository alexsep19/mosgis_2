define([], function () {

    $_DO.create_supply_resource_contract_temperature_charts = function (e) {

        use.block('supply_resource_contract_temperature_charts_popup')
    }

    $_DO.delete_supply_resource_contract_temperature_charts = function (e) {

	if (!e.force) return

	var grid = w2ui[e.target]

	grid.lock()

	query({type: 'supply_resource_contract_temperature_charts', action: 'delete', id: grid.getSelection() [0]}, {}, function () {
	    use.block('supply_resource_contract_temperature_charts')
	})
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

	var data = clone($('body').data('data'))

	done(data)

    }

})