define([], function () {

    $_DO.create_settlement_doc_payments = function (e) {

        use.block('settlement_doc_payments_popup')
    }

    $_DO.delete_settlement_doc_payments = function (e) {

        if (!e.force) return

        var grid = w2ui[e.target]

        grid.lock()

        query({type: 'settlement_doc_payments', action: 'delete', id: grid.getSelection() [0]}, {}, function () {
            use.block('settlement_doc_payments')
        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        var data = clone($('body').data('data'))

        done(data)

    }

})