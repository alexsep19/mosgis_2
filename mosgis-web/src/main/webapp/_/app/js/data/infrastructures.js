define ([], function () {

	$_DO.create_infrastructure = function (e) {
        $_SESSION.set ('record', {})
		use.block ('infrastructures_new')
	}

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'infrastructures', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 
        
    }
    
})