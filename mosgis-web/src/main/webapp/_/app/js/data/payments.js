define ([], function () {

    $_DO.create_account_payments = function (e) {

        function done() {
            use.block('payments')
        }

        $('body').data('accounts_popup.post_data', {search: [
                {field: 'id_ctr_status', operator: 'in', value: [{id: "11"}, {id: "40"}]},
            ], searchLogic: "AND"})

        $('body').data('accounts_popup.callback', function (r) {

            if (!r)
                return done()

            $_SESSION.set('record', {
                uuid_account: r.uuid,
            })

            use.block('payment_new')
        })

        use.block('accounts_popup')
    }

    $_DO.create_payment_document_payments = function (e) {


        function done() {
            use.block('payments')
        }

        $('body').data('payment_documents_popup.post_data', {search: [
                {field: 'id_ctr_status', operator: 'in', value: [{id: "11"}, {id: "40"}]},
            ], searchLogic: "AND"})

        $('body').data('payment_documents_popup.callback', function (r) {

            if (!r)
                return done()

            $_SESSION.set('record', {
                uuid_payment_document: r.uuid,
                year: r.year,
                month: r.month,
            })

            use.block('payment_new')
        })

        use.block('payment_documents_popup')

    }

    return function (done) {

        w2ui ['service_payments_layout'].unlock ('main')

        var data = {}

        query ({type: 'payments', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (var i in d) data [i] = d [i]

            data = clone (data)

            data._can = {
                create: true
            }

            $('body').data('data', data)

            done (data)


        })

    }

})