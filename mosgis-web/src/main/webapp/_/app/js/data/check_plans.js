define ([], function () {

	$_DO.create_check_plans = function () {

		$_SESSION.set ('record', {})
		use.block ('check_plans_new')		

	}

    return function (done) {        
        
        var layout = w2ui ['supervision_layout']
            
        if (layout) layout.unlock ('main')
        
        done ({})
        
    }
    
})