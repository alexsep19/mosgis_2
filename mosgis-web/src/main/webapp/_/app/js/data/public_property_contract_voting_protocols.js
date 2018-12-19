define ([], function () {

    $_DO.create_public_property_contract_voting_protocols = function (e) {

        use.block ('public_property_contract_voting_protocols_new')

    }    

    $_DO.delete_public_property_contract_voting_protocols = function (e) {    

        if (!e.force) return

        e.preventDefault ()

        var grid = w2ui [e.target]
        var r = grid.get (grid.getSelection () [0])

        query ({
            type:   'public_property_contract_voting_protocols',
            id:     r ['m2m.uuid'],
            action: 'delete',
        }, {}, reload_page)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})