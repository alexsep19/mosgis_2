define ([], function () {

    return function (done) {

        w2ui ['service_payments_layout'].unlock ('main')

        var data = clone($('body').data('data') || {})

        done (data)

    }

})