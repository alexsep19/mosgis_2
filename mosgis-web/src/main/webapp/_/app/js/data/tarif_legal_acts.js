define ([], function () {

    var grid_name = 'tarif_legal_acts_grid'

    $_DO.create_tarif_legal_acts = function (e) {

        use.block('tarif_legal_acts_popup')
    }

    $_DO.delete_tarif_legal_acts = function (e) {

        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)

        query({

            type: 'tarif_legal_acts',
            id: r['tla.uuid'],
            action: 'delete',

        }, {}, function(){
            use.block('tarif_legal_acts')
        })

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone($('body').data('data'))

        query ({type: 'legal_acts', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            data._can = {
                create: $_USER.has_nsi_20(7, 10)
            }
            data._can.edit = data._can.delete = data._can.create

            done (data)
        })
    }

})