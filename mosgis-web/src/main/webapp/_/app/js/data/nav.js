define ([], function () {

/*
    $_DO.open_widgets_nav = function () {
        openTab ('/widgets', '/widgets')
    }
*/
    $_DO.open_users_nav = function () {
        openTab ('/users', '/users')
    }
    
    $_DO.open_user_password_nav = function () {
        use.block ('user_password')
    }
        
    $_DO.open_settings_nav = function () {
        use.block ('user_own_options')
    }

    $_DO.logout_nav = function () {
            
        query ({type: 'sessions', action: 'delete'}, {}, $.noop, $.noop)
               
        $_SESSION.end ()

        redirect ('/mos/')

    }

    return function (done) {
    
        var data = {_can: {
            open_widgets: true,
            open_users: ($_USER.role == 'admin'),
            open_user_password: ($_USER.id_voc_user_domain == 1 && $_USER.role != 'admin'),            
            open_settings: true,
            open_help: true,
            logout: true            
        }}

        done (data)
        
    }
    
})