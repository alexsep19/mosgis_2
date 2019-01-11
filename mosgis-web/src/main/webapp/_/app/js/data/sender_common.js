define ([], function () {

    var form_name = 'sender_common_form'
    
    $_DO.cancel_sender_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'senders'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_sender_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_sender_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.label) die ('label', 'Укажите, пожалуйста, сокращённое наименование информационной системы')
        v.label_full = v.label_full || v.label

        query ({type: 'senders', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.set_password_sender_common = function (e) {

        $_SESSION.set ('record', w2ui ['sender_common_form'].record)

        use.block ('sender_password')

    }    
    
    $_DO.delete_sender_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'senders', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.unlock_sender_common = function (e) {   
        if (!confirm ('Разблокировать эту запись, Вы уверены?')) return
        query ({type: 'senders', action: 'unlock'}, {}, reload_page)
    }
    
    $_DO.lock_sender_common = function (e) {   
        if (!confirm ('Блокировать эту запись, Вы уверены?')) return
        query ({type: 'senders', action: 'lock'}, {}, reload_page)
    }
    
    $_DO.choose_tab_sender_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('sender_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('sender_common.active_tab') || 'sender_common_log'

        data.__read_only = 1
        
        add_vocabularies (data, {vc_actions: 1})
        
        $('body').data ('data', data)
        
        var it = data.item

        it._can = {cancel: 1}

        if (!it.is_deleted && $_USER.role.admin) {

            if (it.is_locked) {
                it._can.unlock = 1
            }
            else {
                it._can.edit = 1
                it._can.lock = 1
                it._can.set_password = 1
            }

            it._can.update = it._can.edit

        }

        done (data)

    }

})