define([], function () {

    $_DO.create_supply_resource_contract_objects = function (e) {

        use.block('supply_resource_contract_objects_new')
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        query({type: 'supply_resource_contract_objects', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies(d, d)

            var data = clone($('body').data('data'))

            for (k in d) {
                data[k] = d[k]
            }

            done(data)
        })

    }

})