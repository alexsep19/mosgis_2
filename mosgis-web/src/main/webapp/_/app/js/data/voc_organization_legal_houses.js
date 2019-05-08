define ([], function () {

    return function (done) {        
        
        var layout = w2ui ['voc_organization_legal_layout']
            
        if (layout) layout.unlock ('main')
        
        done ({})
        
    }
    
})