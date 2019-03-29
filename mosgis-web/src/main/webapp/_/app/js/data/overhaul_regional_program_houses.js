define ([], function () {

	$_DO.create_overhaul_regional_program_house = function (e) {
        $_SESSION.set ('record', {})
		use.block ('overhaul_regional_program_houses_new')
	}

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')

        done (clone ($('body').data ('data')))
        
    }
    
})