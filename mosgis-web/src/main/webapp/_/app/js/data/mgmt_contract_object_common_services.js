define ([], function () {

    $_DO.create_mgmt_contract_object_common_services = function (e) {

        use.block ('mgmt_contract_object_common_service_' + e.target.split ('_') [1] + '_new')

    }   
    
    $_DO.edit_mgmt_contract_object_common_services = function (e) {

        use.block ('mgmt_contract_object_common_service_popup')

    }   

    $_DO.delete_mgmt_contract_object_common_services = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'contract_object_services', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['passport_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})