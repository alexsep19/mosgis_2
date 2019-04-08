define ([], function () {

	$_DO.create_overhaul_regional_program_house = function (e) {
        $_SESSION.set ('record', {})
		use.block ('overhaul_regional_program_houses_new')
	}

	$_DO.delete_overhaul_regional_program_house = function (e) {

		if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

		var grid = w2ui ['overhaul_regional_program_houses_grid']
        query ({
        
            type:   'overhaul_regional_program_houses', 
            id:     grid.getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
	}

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')

        done (clone ($('body').data ('data')))
        
    }
    
})