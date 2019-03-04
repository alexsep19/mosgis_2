define ([], function () {

	$_DO.import_houses = function (e) {
        use.block ('houses_import_popup')
    }

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')

    	query ({type: 'houses', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })
        
    }
    
})