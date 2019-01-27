define([], function () {

    $_DO.edit_supply_resource_contract_object_other_quality_levels = function (e) {

        var grid = w2ui [e.target]

        var r = grid.get(e.recid)

        $_SESSION.set('record', r)

        use.block('supply_resource_contract_object_other_quality_levels_popup')
    }

    $_DO.delete_supply_resource_contract_object_other_quality_levels = function (e) {

        if (!e.force)
            return

        var grid = w2ui [e.target]

        grid.lock()

        query({type: 'supply_resource_contract_other_quality_levels', id: grid.getSelection() [0], action: 'delete'}, {}, function () {

            use.block('supply_resource_contract_object_other_quality_levels')

        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        var data = clone($('body').data('data'))

        query(
            {type: 'supply_resource_contract_other_quality_levels', part: 'vocs', id: undefined}, {data: {}}, function (d) {

            add_vocabularies(d, d)

            for (k in d) {
                data[k] = d[k]
            }

            done(data)
        })

    }

})