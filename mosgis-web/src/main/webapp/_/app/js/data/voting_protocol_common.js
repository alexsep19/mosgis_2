define ([], function () {

    var form_name = 'voting_protocol_common_form'
    
    $_DO.cancel_voting_protocol_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'voting_protocols'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_voting_protocol_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_voting_protocol_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                
        var re = /^[А-ЯЁ][а-яё\-]*$/
        
        //if (!v.surname) die ('surname', 'Укажите, пожалуйста, фамилию')
        //if (!re.test (v.surname)) die ('surname', 'Фамилия содержит не корректные символы')
        //if (!v.firstname) die ('firstname', 'Укажите, пожалуйста, имя')
        //if (!re.test (v.firstname)) die ('firstname', 'Имя содержит некорректные символы')
        //if (v.patronymic && (!re.test (v.patronymic) || !/[ач]$/.test (v.patronymic))) die ('patronymic', 'Отчество указано некорректно')

        //if (v.is_female=="") v.is_female=null
        
        //var re_dt = /^\d\d\d\d\-\d\d-\d\d$/
        
        //if (v.birthdate && !re_dt.test (v.birthdate)) die ('birthdate', 'Некорректный формат даты')
        
        //if (v.code_vc_nsi_95) {
            //if (v.number_===null) die ('number_', 'Пожалуйста, укажите номер документа')
            //if (!v.issuedate) die ('issuedate', 'Пожалуйста, укажите дату выдачи документа')
            //if (!re_dt.test (v.issuedate)) die ('issuedate', 'Некорректный формат даты')
            //if (!v.issuer) die ('issuer', 'Пожалуйста, укажите, кем выдан документ')
        //}

        query ({type: 'voting_protocols', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_voting_protocol_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'voting_protocols', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_voting_protocol_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'voting_protocols', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_voting_protocol_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voting_protocol_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('voting_protocol_common.active_tab') || 'voting_protocol_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        permissions = 0

        if (!data.item.is_deleted && data.cach && data.cach.id_ctr_status_gis == 10) {
            permissions = 1
        }

        data.item._can = $_USER.role.admin ? {} : 
        {
            edit: permissions,
            update: 1,
            cancel: 1,
            delete: permissions,
        }

        done (data)
        
    }

})