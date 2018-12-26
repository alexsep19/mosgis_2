define ([], function () {

    var form_name = 'working_list_common_form'    

    $_DO.cancel_working_list_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'working_lists'}, {}, function (data) {

            var it = data.item
            
            it.dt_from = it.dt_from.substr (0, 10)
            it.dt_to   = it.dt_to.substr (0, 10)

            data.__read_only = true
            
            $_F5 (data)

        })

    }
    
    $_DO.edit_working_list_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_working_list_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (v.dt_from > v.dt_to) die ('dt_to', 'Окончание периода перечня не может быть раньше начала. Укажите корректные значения.')

        query ({type: 'working_lists', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_working_list_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'working_lists', action: 'delete'}, {}, reload_page)
    }
        
    $_DO.approve_working_list_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'working_lists', action: 'approve'}, {}, reload_page)
    }

    $_DO.choose_tab_working_list_common = function (e) {
    
        var name = e.tab.id
                        
        var layout = w2ui ['passport_layout']

        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('working_list_common.active_tab', name)

        if (/^working_list_common_plan_/.test (name)) {
            $_SESSION.set ('year', name.substr (-4))
            name = 'working_list_common_plan'
        }

        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        data.begins = data.periods.map (function (i) {return {
            id: i.id.substr (0, 8) + '01',
            text: i.text
        }})
        
        data.ends   = clone (data.periods).reverse ()

        data.active_tab = localStorage.getItem ('working_list_common.active_tab') || 'working_list_common_items'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']        
       
        it.gis_status_label = data.vc_gis_status [it.id_ctr_status]

        done (data)
        
    }

})