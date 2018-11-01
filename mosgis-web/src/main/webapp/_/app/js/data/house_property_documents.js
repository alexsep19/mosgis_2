define ([], function () {
    
/*    
    $_DO.create_house_docs = function (e) {
            
        use.block ('house_doc_new')
    
    }
*/    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var house = $('body').data ('data')

        done (house);

    }

})