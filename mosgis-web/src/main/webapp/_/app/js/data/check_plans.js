define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['supervision_layout']
            
        if (layout) layout.unlock ('main')
        
        done ({})
        
    }
    
})