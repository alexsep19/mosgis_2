define ([], function () {

	$_DO.create_overhaul_address_program = function (e) {
        $_SESSION.set ('record', {})
		use.block ('overhaul_address_programs_new')
	}

    return function (done) {
        
        var layout = w2ui ['overhauls_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'overhaul_address_programs', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 
        
    }
    
})