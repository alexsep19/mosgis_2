define ([], function () {

    $_DO.create_tarif_coeffs = function (e) {

        use.block('tarif_coeffs_popup')
    }

    $_DO.edit_tarif_coeffs = function (e) {

        $_SESSION.set('record', this.get(e.recid))

        use.block('tarif_coeffs_popup')

    }

    $_DO.delete_tarif_coeffs = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'tarif_coeffs',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, function(){
            use.block('tarif_coeffs')
        })

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone($('body').data('data'))

        data._can = {
            create: $_USER.has_nsi_20(7, 10)
        }
        data._can.edit = data._can.delete = data._can.create

        done (data);

    }

})