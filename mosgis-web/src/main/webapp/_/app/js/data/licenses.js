define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'licenses', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })
        
    }
    
})