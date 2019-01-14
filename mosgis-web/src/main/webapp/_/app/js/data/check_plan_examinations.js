define ([], function () {

	$_DO.create_check_plan_examination = function () {

		$_SESSION.set ('record', {})
		use.block ('planned_examinations_new')		

	}

	$_DO.delete_check_plan_examination = function (e) {

        if (!confirm ('Удалить проверку?')) return

		var grid = w2ui ['check_plan_examinations_grid']
    
    	var id = grid.getSelection () [0]

        var tia = {
            type:   'planned_examinations', 
            id:     id,
            action: 'delete'
        }

        query (tia, {}, function () {
            grid.reload (function () {
                grid.refresh ()            
                grid.selectNone ()
            })
        })
	}

    return function (done) {        
        
        var layout = w2ui ['passport_layout']
            
        if (layout) layout.unlock ('main')
        
        done ({})
        
    }
    
})