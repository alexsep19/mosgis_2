define ([], function () {

    $_DO.create_legal_acts = function (e) {

        use.block('legal_act_new')

    }

    $_DO.download_legal_acts = function (e) {

        var box = $('body')

        var r = this.get(e.recid)

        function label(cur, max) {
            return String(Math.round(100 * cur / max)) + '%'
        }

        w2utils.lock(box, label(0, 1))

        download({

            type: 'legal_acts',
            id: e.recid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {
                $('.w2ui-lock-msg').html('<br><br>' + label(cur, max))
            },

            onload: function () {
                w2utils.unlock(box)
            },

        })

    }

    $_DO.delete_legal_acts = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'legal_acts',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, reload_page)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = {}

        query ({type: 'legal_acts', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            done (data);

        })

    }

})