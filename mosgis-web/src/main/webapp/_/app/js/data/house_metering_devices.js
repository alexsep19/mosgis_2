define ([], function () {
    
    $_DO.create_person_house_metering_devices = function (e) {
            
//        use.block ('property_document_person_new')
    
    }

    $_DO.create_org_house_metering_devices = function (e) {
            
//        use.block ('property_document_org_new')
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = $('body').data ('data')

        done (data);

    }

})