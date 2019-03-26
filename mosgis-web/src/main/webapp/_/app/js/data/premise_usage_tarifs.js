define ([], function () {

    $_DO.create_premise_usage_tarifs = function (e) {

        use.block('premise_usage_tarif_new')

    }

    $_DO.delete_premise_usage_tarifs = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'premise_usage_tarifs',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, reload_page)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = {}

        query ({type: 'premise_usage_tarifs', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            data._can = {
                create: $_USER.has_nsi_20(7, 10)
            }

            done (data);

        })

    }

})