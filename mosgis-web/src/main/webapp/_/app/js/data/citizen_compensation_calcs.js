define ([], function () {

    $_DO.create_citizen_compensation_calcs = function (e) {

        $_SESSION.set ('record', {})

        use.block('citizen_compensation_calc_popup')
    }

    $_DO.edit_citizen_compensation_calcs = function (e) {

        var grid = w2ui [e.target]

        var r = grid.get(e.recid)

        $_SESSION.set('record', r)

        use.block('citizen_compensation_calc_popup')
    }

    $_DO.delete_citizen_compensation_calcs = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'citizen_compensation_calcs',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, function(data) {
            use.block('citizen_compensation_calcs')
        })

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone($('body').data('data'))

        query ({type: 'citizen_compensation_calcs', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            done (data);

        })

    }

})