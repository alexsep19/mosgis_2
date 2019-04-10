define ([], function () {

    $_DO.create_account_payments = function (e) {           

        use.block ('payment_document_new')

    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = $('body').data ('data')

        query ({type: 'payment_documents', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (var i in d) data [i] = d [i]
            
            data = clone (data)
/*            
            data.periods = []
            
            var dt = new Date ()
            dt.setDate (1)            
            var year_limit = dt.getFullYear () - 3
            
            while (data.periods.length < 36 && dt.getFullYear () > year_limit) {
            
                data.periods.push ({
                    id: dt.toJSON ().slice (0, 7),
                    text: w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear ()
                })
                
                dt.setMonth (dt.getMonth () - 1)

            }
*/
            done (data)

        })

    }

})