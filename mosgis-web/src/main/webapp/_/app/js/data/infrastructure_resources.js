define ([], function () {

	$_DO.create_infrastructure_resource = function (e) {
        $_SESSION.set ('record', {})
        use.block ('infrastructure_resources_popup')
    }

    $_DO.edit_infrastructure_resource = function (e) {
        var g = w2ui ['infrastructure_resources_grid']
        $_SESSION.set ('record', g.get (g.getSelection ()[0]))
        use.block ('infrastructure_resources_popup')
    }

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))
        
        done (data)
        
    }
    
})