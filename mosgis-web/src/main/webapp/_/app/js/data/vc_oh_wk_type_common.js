define ([], function () {

    var form_name = 'vc_oh_wk_type_common_form'
    
    $_DO.choose_tab_vc_oh_wk_type_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vc_oh_wk_type_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('vc_oh_wk_type_common.active_tab') || 'vc_oh_wk_type_common_log'
                        
        done (data)
        
    }

})