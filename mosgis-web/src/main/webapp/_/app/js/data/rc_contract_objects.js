define([], function () {

    $_DO.create_rc_contract_objects = function (e) {

        use.block('rc_contract_objects_popup')
    }

    $_DO.delete_rc_contract_objects = function (e) {

        if (!e.force)
            return

        var grid = w2ui[e.target]

        grid.lock()

        query({type: 'rc_contract_objects', action: 'delete', id: grid.getSelection() [0]}, {}, function () {
            use.block('rc_contract_objects')
        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        query({type: 'rc_contract_objects', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies(d, d)

            var data = clone($('body').data('data'))

            for (k in d) {
                data[k] = d[k]
            }

            done(data)
        })

    }

})