define ([], function () {
    
    return function (data, view) {
        
        fill (view, data, $('body') )

        $('div.login-inner').w2form ({
        
            name   : 'form',            
            header : 'Вход в систему',
                        
            fields : [
                { field: 'login',     type: 'text',     },
                { field: 'password',  type: 'password', },
            ],

            actions: {            
                'execute': {caption: 'Войти', onClick: $_DO.execute_login},                                
            }
            
        })
        
        $('input[name=login]').keypress    (function (e) {if (e.which == 13) $('input[name=password]').focus ()})
        $('input[name=password]').keypress (function (e) {if (e.which == 13) $_DO.execute_login ()})
        
        $('#reg').attr ({href: sessionStorage.getItem ('staticRoot') + "/libs/mosgis/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%B0%D0%BD%D0%BA%D0%B5%D1%82%D1%8B_%D0%BD%D0%B0_%D0%B4%D0%BE%D1%81%D1%82%D1%83%D0%BF_%D0%BA_%D0%BF%D0%BE%D0%B4%D1%81%D0%B8%D1%81%D1%82%D0%B5%D0%BC%D0%B5.docx"})
    }

})