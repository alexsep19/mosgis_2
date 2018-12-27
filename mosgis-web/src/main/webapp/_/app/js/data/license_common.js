define ([], function () {
    
    $_DO.choose_tab_license_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('license_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        
       
        var data = clone ($('body').data ('data'))
        
        data.active_tab = localStorage.getItem ('license_common.active_tab') || 'license_common_documents'

        var it = data.item
        
        it.status_label     = data.vc_license_status [it.id_status]

        done (data)
        
    }

})