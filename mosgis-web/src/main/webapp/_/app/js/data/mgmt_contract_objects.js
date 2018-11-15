define ([], function () {

    $_DO.create_mgmt_contract_objects = function (e) {            
    
        use.block ('mgmt_contract_object_new')   
        
    }

    $_DO.add_mgmt_contract_objects = function (e) {
    
        use.block ('mgmt_contract_object_popup')
        
    }   

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})