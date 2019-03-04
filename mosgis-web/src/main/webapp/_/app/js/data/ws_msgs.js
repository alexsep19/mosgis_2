define ([], function () {     

    return function (done) {        
        
        var layout = w2ui ['integration_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'ws_msgs', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })
                        
    }
    
})