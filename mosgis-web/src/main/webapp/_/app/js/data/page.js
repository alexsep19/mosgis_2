define ([], function () {

    $_DO.logout_page = function (e) {    
    
        if (!confirm ('Выйти из системы?')) return
    
        query ({type: 'sessions', action: 'delete', id: undefined}, {}, $.noop, $.noop)        
        
        $_SESSION.end ()
        
        redirect ('/')        

    }

    return function (done) {        
                
        done ({
            user_label: $_USER.label,
            org_label: $_USER.label_org,
        })
        
    }
    
})