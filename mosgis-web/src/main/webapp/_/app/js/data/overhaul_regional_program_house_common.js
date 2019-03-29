define ([], function () {

    var form_name = 'overhaul_regional_program_house_common_form'
    
    $_DO.delete_overhaul_regional_program_house_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'overhaul_regional_program_documents', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_overhaul_regional_program_house_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_regional_program_house_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item

        data.active_tab = localStorage.getItem ('overhaul_regional_program_house_common.active_tab') || 'overhaul_regional_program_house_common_log'

        data.__read_only = 1
                        
        done (data)
        
    }

})