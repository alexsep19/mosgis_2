define ([], function () {

    //var plan_status = ['Проект', 'Подписан']

    var form_name = 'planned_examination_common_form'    

    $_DO.cancel_planned_examination_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'planned_examinations'}, {}, function (data) {
            data.__read_only = true
            $_F5 (data)
        })

    }
    
    $_DO.edit_planned_examination_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_planned_examination_common = function (e) {
    
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

        query ({type: 'planned_examinations', action: 'update'}, {data: v}, reload_page)

    } 
    
    $_DO.delete_check_plan_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'planned_examinations', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_planned_examination_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('planned_examination_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.__read_only = 1
        
        done (data)                  
        
    }

})