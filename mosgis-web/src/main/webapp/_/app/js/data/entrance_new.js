define ([], function () {

    $_DO.update_entrance_new = function (e) {

        w2ui ['entrance_new_popup_form'].values ()
        
        var no = w2ui ['entrance_new_popup_form'].values ().no
        
        if (no == null) no = ''
                
        var nos = no.trim ().split (/[\s\,\;]+/).filter (function (s) {return s.length > 0})

        if (nos.length == 0) die ('no', 'Укажите, пожалуйста, хотя бы один номер подъезда')
        
        var cnt = {}; $.each (nos, function () {cnt [this] = 1 + (cnt [this] | 0)})
        
        nos = []
        
        for (no in cnt) {

            if (cnt [no] > 1) die ('no', 'Вы указали №' + no + ' более одного раза')
            nos.push (no)

        }
        
        query ({type: 'entrances', id: null, action: 'create'}, {data: {uuid_house: $_REQUEST.id, nos: nos}}, reload_page)
    
    }

    return function (done) {
    
        var house = $('body').data ('data')

        done (house)
        
    }
    
})