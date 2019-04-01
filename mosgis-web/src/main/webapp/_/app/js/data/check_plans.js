define ([], function () {

	$_DO.create_check_plans = function () {

		$_SESSION.set ('record', {})
		use.block ('check_plans_new')		

	}

	$_DO.delete_check_plans = function (e) {

        if (!confirm ('Удалить план?')) return

		var grid = w2ui ['check_plans_grid']
    
    	var id = grid.getSelection () [0]

        var tia = {
            type:   'check_plans', 
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
	
	$_DO.import_check_plans = function (e) {
		use.block ('check_plans_import')
	}

    return function (done) {        
        
        var layout = w2ui ['supervision_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'check_plans', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })
        
    }
    
})