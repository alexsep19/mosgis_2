define ([], function () {
    
    $_DO.create_person_house_property_documents = function (e) {
            
        use.block ('property_document_person_new')
    
    }

    $_DO.create_person_house_property_documents = function (e) {
            
        use.block ('property_document_org_new')
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var house = $('body').data ('data')

        done (house);

    }

})