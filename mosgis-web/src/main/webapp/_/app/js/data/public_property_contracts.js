define ([], function () {

    return function (done) {
    
        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

//        query ({type: 'public_property_contracts', part: 'vocs', id: undefined}, {}, function (data) {

//            add_vocabularies (data, data)

            var data = {}

            $('body').data ('data', data)

            done (data)

//        }) 

    }

})