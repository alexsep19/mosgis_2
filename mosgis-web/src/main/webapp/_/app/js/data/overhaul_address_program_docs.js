define ([], function () {

	$_DO.create_overhaul_address_program_doc = function (e) {
        $_SESSION.set ('record', {})
		use.block ('overhaul_address_program_docs_new')
	}

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'overhaul_address_program_documents', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            data.item = clone ($('body').data ('data')).item

            $('body').data ('data', data)

            done (data)

        }) 
        
    }
    
})