define([], function () {

    $_DO.create_supply_resource_contract_object_subjects = function (e) {

        use.block('supply_resource_contract_object_subjects_popup')
    }

    $_DO.delete_supply_resource_contract_object_subjects = function (e) {

	if (!e.force) return

	var grid = w2ui[e.target]

	grid.lock()

	query({type: 'supply_resource_contract_object_subjects', action: 'delete', id: grid.getSelection() [0]}, {}, function () {
	    use.block('supply_resource_contract_object_subjects')
	})
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

	var data = clone($('body').data('data'))

	done(data)
    }

})