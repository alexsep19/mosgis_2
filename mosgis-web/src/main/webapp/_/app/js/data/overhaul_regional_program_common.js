define ([], function () {

    var form_name = 'overhaul_regional_program_common_form'

    $_DO.cancel_overhaul_regional_program_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'overhaul_regional_programs'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item

            $_F5 (data)

        })

    }
    
    $_DO.edit_overhaul_regional_program_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_overhaul_regional_program_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var it = $('body').data ('data').item
        
        var f = w2ui [form_name]
        var v = f.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        
        if (!v.startyear) die ('startyear', 'Пожалуйста, укажите год начала периода реализации')
        if (!reg_year.test (v.startyear)) die ('startyear', 'Пожалуйста, укажите корректное значение года начала периода реализации')
        if (!v.endyear) die ('endyear', 'Пожалуйста, укажите год окончания периода реализации')
        if (!reg_year.test (v.endyear)) die ('endyear', 'Пожалуйста, укажите корректное значение года окончания периода реализации')
        if (v.endyear < v.startyear) die ('endyear', 'Год окончания реализации не может быть раньше года начала реализации')    
        if (!v.programname) die ('programname', 'Пожалуйста, укажите наименование')
                                               
        query ({type: 'overhaul_regional_programs', action: 'update'}, {data: v}, reload_page)
        
    }
    
    $_DO.delete_overhaul_regional_program_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'overhaul_regional_programs', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_overhaul_regional_program_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_regional_program_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item

        data.active_tab = localStorage.getItem ('overhaul_regional_program_doc_common.active_tab') || 'overhaul_regional_program_doc_common_log'

        data.__read_only = 1
                        
        done (data)
        
    }

})