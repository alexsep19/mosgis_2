define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'infrastructures', part: 'vocs', id: undefined}, {}, function (data) {

        	console.log (data)

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 
        
    }
    
})