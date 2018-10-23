define ([], function () {

    $_DO.update_vc_person_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        var re = /^[А-ЯЁ][а-яё\-]*$/
        
        if (!v.surname) die ('surname', 'Укажите, пожалуйста, фамилию')
        if (!re.test (v.surname)) die ('surname', 'Фамилия содержит не корректные символы')
        if (!v.firstname) die ('firstname', 'Укажите, пожалуйста, имя')
        if (!re.test (v.firstname)) die ('firstname', 'Имя содержит некорректные символы')
        
        if (v.patronymic && (!re.test (v.patronymic) || !/[ач]$/.test (v.patronymic))) die ('patronymic', 'Отчество указано некорректно')
        
        if (v.is_female=="") v.is_female=null
        
        var tia = {type: 'vc_persons'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['vc_persons_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу физического лица?').yes (function () {openTab ('/vc_person/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})