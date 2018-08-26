define ([], function () {

    $_DO.update_user_password = function (e) {
    
        var data = w2ui ['passwordForm'].record
        
        if (!data.p1) return alert ('Вы не ввели пароль');
        if (!data.p2) return alert ('Ввод пароля необходимо повторить');
        
        if (data.p1 != data.p2) return alert ('Вам не удалось ввести одно значение пароля дважды');

        $_REQUEST._secret = ['password']
        
        var record = $_SESSION.get ('record')
        
        query ({type: 'voc_users', id: record.uuid, action: 'set_password'}, {data: {password: data.p1}}, function (data) {
        
            alert ('Пароль установлен')

            record.f ? w2popup.close () : reload_page ()
        
        })

    }

    return function (done) {
                    
        done ($('body').data ('data'))
            
    }
    
})