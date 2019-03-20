define ([], function () {

    $_DO.create_account_payment_documents = function (e) {           
        
//        use.block ('account_' + e.target + '_new')
        
    }  
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        query ({type: 'payment_documents', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (var i in d) data [i] = d [i]

            done (data)

        })

    }

})