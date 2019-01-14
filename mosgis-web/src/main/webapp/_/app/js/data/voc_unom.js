define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['integration_layout']
            
        if (layout) layout.unlock ('main')
        
        var data = {}
                        
        $('body').data ('data', data)
                        
        done (data)
                        
    }
    
})