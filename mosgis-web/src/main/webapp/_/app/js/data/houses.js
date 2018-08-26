define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')
        
        done ({})
        
    }
    
})