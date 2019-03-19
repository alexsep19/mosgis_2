define ([], function () {

    $_DO.delete_voc_organization_legal_users = function (e) {
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({        
            type:   'voc_users', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',            
        }, {}, reload_page)

    }

    $_DO.create_voc_organization_legal_users = function (e) {
        $_SESSION.set ('record', {})
        use.block ('voc_user_popup')
    }    
    
    $_DO.edit_voc_organization_legal_users = function (e) {
        var grid = w2ui [e.target]
        $_SESSION.set ('record', grid.get (e.recid))
        use.block ('voc_user_popup')
    }  
    
    $_DO.lock_voc_user = function (e) {
        $_SESSION.set ('record', {})
        use.block ('lock_voc_user_popup')
    }    

    $_DO.unlock_voc_user = function (e) {
        
        var grid = w2ui ['voc_organizations_grid']
        var id = grid.getSelection () [0]
        
        query ({        
            type:   'voc_users', 
            id:     id,
            action: 'unlock',            
        }, {}, reload_page)
        
    } 

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'voc_users', part: 'vocs', id: undefined}, {}, function (data) {

            done (data)
        
        }) 
                
    }
    
})