define ([], function () {

    $_DO.set_password_voc_user_popup = function (e) {

        $_SESSION.set ('record', w2ui ['voc_user_form'].record)

        use.block ('user_password')

    }

    $_DO.update_voc_user_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        var re = /^[А-ЯЁ][а-яё\-]*$/

        if (!v.f) die ('f', 'Укажите, пожалуйста, фамилию оператора')
        if (!re.test (v.f)) die ('f', 'Фамилия содержит некорректные символы')
        
        if (!v.i) die ('i', 'Укажите, пожалуйста, имя оператора')
        if (!re.test (v.i)) die ('i', 'Имя содержит некорректные символы')

        if (v.o && (!re.test (v.o) || !/[ач]$/.test (v.o))) die ('o', 'Отчество указано некорректно')
        
        var tia = {type: 'voc_users'}        
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page
        
        if (tia.action == 'create') {
        
            v.uuid_org = $_REQUEST.id
            
            done = function set_pass (data) {        
                $_SESSION.set ('record', data)
                use.block ('user_password')            
            }
            
        }        

        query (tia, {data: v}, done)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})