define ([], function () {

    return function (done) {

        w2ui ['service_payments_layout'].unlock ('main')

        var data = {}

        query ({type: 'payment_documents', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (var i in d) data [i] = d [i]
            
            data = clone (data)

            done (data)

        })

    }

})