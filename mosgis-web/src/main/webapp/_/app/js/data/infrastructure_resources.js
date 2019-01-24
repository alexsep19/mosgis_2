define ([], function () {

	$_DO.create_infrastructure_resource = function (e) {
        $_SESSION.set ('record', {})
        use.block ('infrastructure_resources_new')
    }

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')
        
        done ($('body').data ('data'))
        
    }
    
})