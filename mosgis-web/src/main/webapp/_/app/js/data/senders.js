define ([], function () {

    $_DO.create_senders = function (e) {
        $_SESSION.set ('record', {})
        use.block ('sender_popup')
    }        

    return function (done) {        
        
        var layout = w2ui ['integration_layout']
            
        if (layout) layout.unlock ('main')
        
        var data = {}
                        
        $('body').data ('data', data)
                        
        done (data)
                        
    }
    
})