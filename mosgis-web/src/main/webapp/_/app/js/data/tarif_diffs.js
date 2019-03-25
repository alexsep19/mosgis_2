define ([], function () {

    $_DO.create_tarif_diffs = function (e) {

        if (!/^create/.test(e.item.id) || !e.subItem || !e.subItem.id) {
            return
        }

        var saved = {
            uuid_tf: $_REQUEST.id,
            id_type: e.subItem.id
        }

        function done() {

            $_SESSION.set('record', {
                uuid_tf   : saved.uuid_tf,
                id_type   : saved.id_type,
                code_diff : saved.code_diff
            })
            use.block('tarif_diffs_popup')
        }

        $('body').data('voc_differentiation_popup.callback', function (r) {

            if (!r)
                return

            saved.code_diff = r.differentiationcode
            saved.id_type = r.differentiationvaluekind

            done()

        })

        $_SESSION.set('voc_differentiation_popup.post_data', {search: [
                {field: 'differentiationvaluekind', operator: 'in', value: [saved.id_type]},
                {field: 'id_tariff_type', operator: 'in', value: ['ResidentialPremisesUsage']}
            ], searchLogic: "AND"})

        use.block('voc_differentiation_popup')
    }

    $_DO.edit_tarif_diffs = function (e) {

        $_SESSION.set('record', this.get(e.recid))

        use.block('tarif_diffs_popup')

    }

    $_DO.delete_tarif_diffs = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'tarif_diffs',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, function(){
            use.block('tarif_diffs')
        })

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone($('body').data('data'))

        query ({type: 'tarif_diffs', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) data [k] = d [k]

            $('body').data('data', data)

            data._can = {
                create: $_USER.has_nsi_20(7, 10)
            }
            data._can.edit = data._can.delete = data._can.create

            done (data);
        })

    }

})