define ([], function () {

    return function (done) {                
        
        query ({type: 'voc_unom', part: 'vocs'}, {}, function (data) {
        
            var layout = w2ui ['integration_layout']

            if (layout) layout.unlock ('main')

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)        
        
        }) 
                                   
    }
    
})