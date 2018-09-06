define ([], function () {
    
    $_DO.create_mgmt_contract_objects = function (e) {
            
        use.block ('mgmt_contract_object_new')
    
    }   
/*
    $_DO.delete_mgmt_contract_objects = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'contract_objects', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }
*/
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})