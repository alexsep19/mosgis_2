define ([], function () {

    $_DO.create_citizen_compensations = function (e) {

        use.block('citizen_compensation_new')

    }

    $_DO.delete_citizen_compensations = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'citizen_compensations',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, reload_page)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = {}

        query ({type: 'citizen_compensations', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            done (data);

        })

    }

})