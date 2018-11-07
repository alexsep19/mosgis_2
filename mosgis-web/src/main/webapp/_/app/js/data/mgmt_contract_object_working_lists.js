define ([], function () {

    $_DO.edit_mgmt_contract_object_working_lists = function (e) {
            
        use.block ('mgmt_contract_object_working_list_popup')
    
    }    
    
    $_DO.create_mgmt_contract_object_working_lists = function (e) {
            
        use.block ('mgmt_contract_object_working_list_new')
    
    }
    
    $_DO.delete_mgmt_contract_object_working_lists = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'working_lists', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})