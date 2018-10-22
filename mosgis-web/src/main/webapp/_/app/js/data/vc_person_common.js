define ([], function () {

    var form_name = 'vc_person_common_form'
    
    $_DO.cancel_vc_person_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'vc_persons'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_vc_person_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_vc_person_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                
        var re = /^[А-ЯЁ][а-яё\-]*$/
        
        if (!v.surname) die ('surname', 'Укажите, пожалуйста, фамилию')
        if (!re.test (v.surname)) die ('surname', 'Фамилия содержит не корректные символы')
        if (!v.firstname) die ('firstname', 'Укажите, пожалуйста, имя')
        if (!re.test (v.firstname)) die ('firstname', 'Имя содержит некорректные символы')
        if (v.patronymic && (!re.test (v.patronymic) || !/[ач]$/.test (v.patronymic))) die ('patronymic', 'Отчество указано некорректно')

        if (v.is_female=="") v.is_female=null
        
        if (v.code_vc_nsi_95) {
            if (v.number_===null) die ('number_', 'Пожалуйста, укажите номер документа')
            if (!v.issuedate) die ('issuedate', 'Пожалуйста, укажите дату выдачи документа')
            if (!v.issuer) die ('issuer', 'Пожалуйста, укажите, кем выдан документ')
        }

        query ({type: 'vc_persons', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_vc_person_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'vc_persons', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_vc_person_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'vc_persons', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_vc_person_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vc_person_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('vc_person_common.active_tab') || 'vc_person_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        data.item._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
//            undelete: data.item.is_deleted,
        }

        done (data)
        
    }

})