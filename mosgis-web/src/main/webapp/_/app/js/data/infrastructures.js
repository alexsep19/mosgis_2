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

            var ref_33_to_3 = []

            data.ref_33_to_3.items.forEach ((x, index, arr) => {

                if (ref_33_to_3.findIndex (y => {
                    return y.code_33 == x.code_33
                }) < 0) {

                    ref_33_to_3.push (data.ref_33_to_3.items.filter (z => z.code_33 == x.code_33).reduce ((accumulator, currentValue, currentIndex, array) => {
                        accumulator.code_3.push (data.vc_nsi_3.items.find (nsi_3 => nsi_3.id == currentValue.code_3))
                        return accumulator
                    }, {code_33: x.code_33, code_3: []}))

                }

            })

            data.ref_33_to_3 = ref_33_to_3

            $('body').data ('data', data)

            done (data)

        }) 
        
    }
    
})