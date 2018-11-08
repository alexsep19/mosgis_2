define ([], function () {

    var form_name = 'working_list_common_form'    

    $_DO.cancel_working_list_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'working_lists'}, {}, function (data) {

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
        query ({type: 'contract_objects', action: 'delete'}, {}, reload_page)
    }
        
    $_DO.choose_tab_working_list_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('working_list_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        data.begins = data.periods.map (function (i) {return {
            id: i.id.substr (0, 8) + '01',
            text: i.text
        }})
        
        data.ends   = clone (data.periods).reverse ()

        data.active_tab = localStorage.getItem ('working_list_common.active_tab') || 'working_list_common_services'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
//        it.status_label     = data.vc_gis_status [it.id_ctr_status]
//        if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
                
        done (data)
        
    }

})