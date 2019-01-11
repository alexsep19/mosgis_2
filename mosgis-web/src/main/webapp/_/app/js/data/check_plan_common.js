define ([], function () {

    var plan_status = ['Проект', 'Подписан']

    var form_name = 'check_plan_common_form'    

    $_DO.cancel_check_plan_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'check_plans'}, {}, function (data) {
            data.__read_only = true
            $_F5 (data)
        })

    }
    
    $_DO.edit_check_plan_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_check_plan_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()

        var re = /^\d{1,12}$/

        if (v.shouldberegistered) {

            if (v.uriregistrationplannumber == null) die ('uriregistrationplannumber', 'Пожалуйста, укажите регистрационный номер')
            if (!re.test (v.uriregistrationplannumber)) die ('uriregistrationplannumber', 'Указан неверный регистрационный номер')

        }

        v.shouldnotberegistered = 1 - v.shouldberegistered
        delete v.shouldberegistered

        query ({type: 'check_plans', action: 'update'}, {data: v}, reload_page)

    } 
    
    $_DO.delete_check_plan_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'check_plans', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_check_plan_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('check_plan_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.item.status_label = plan_status[data.item.sign]

        data.active_tab = localStorage.getItem ('check_plan_common.active_tab') || 'check_plan_common_log'

        data.__read_only = 1
        
        done (data)                  
        
    }

})