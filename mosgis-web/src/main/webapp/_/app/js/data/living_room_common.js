define ([], function () {

    var form_name = 'living_room_common_form'
    
    $_DO.restore_living_room_common = function (e) {
    
        if (!confirm ('Удалить запись об аннулировании объекта?')) return 
        
        query ({
        
            type: 'living_rooms', 
            id: $_REQUEST.id, 
            action: 'update'
            
        }, {data: {
        
            terminationdate: null,
            annulmentinfo: null,
            code_vc_nsi_330: null,
        
        }}, reload_page)        

    }    

    $_DO.annul_living_room_common = function (e) {
    
        use.block ('annul_popup')

    }    
    
    $_DO.cancel_living_room_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'living_rooms'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_living_room_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.is_annuled) {
            alert ('Поскольку данный объект жилого фонда аннулирован, редактирование невозможно.')
            return e.preventDefault ()
        }       

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_living_room_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (v.roomnumber == null || v.roomnumber == '') die ('roomnumber', 'Укажите, пожалуйста, номер комнаты')
        
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.roomnumber)) die ('roomnumber', 'Некорректный номер комнаты')

        if (parseFloat (v.square || '0') < 0.01) die ('square', 'Необходимо указать размер плошади')

        query ({type: 'living_rooms', action: 'update'}, {data: v}, reload_page)

    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.__read_only = 1
        
        data.item._can = {
            edit: 1,
            update: 1,
            cancel: 1,
            annul: 1 - data.item.is_annuled,
            restore: data.item.is_annuled,
        }

        done (data)
        
    }

})