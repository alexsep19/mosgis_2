define ([], function () {

    $_DO.create_person_public_property_contracts = function (e) {
            
        use.block ('public_property_contract_person_new')
    
    }

    $_DO.create_org_public_property_contracts = function (e) {
            
        use.block ('public_property_contract_org_new')
    
    }

    return function (done) {
    
        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')
        
        query ({type: 'public_property_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)
            
            $('body').data ('data', data)

            done (data)

        }) 

    }

})