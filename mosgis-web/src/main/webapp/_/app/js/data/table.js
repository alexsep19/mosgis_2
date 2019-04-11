define ([], function () {

    return function (done) {

        query ({type: 'tables'}, {}, function (data) {        
    
            done (data)

        })
        
    }

})