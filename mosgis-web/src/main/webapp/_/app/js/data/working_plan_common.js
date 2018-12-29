define ([], function () {

    var form_name = 'working_plan_common_form'    
/*        
    $_DO.approve_working_plan_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'working_lists', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.unapprove_working_plan_common = function (e) {
        if (!confirm ('Отменить публикацию этих данных в ГИС ЖКХ?')) return
        query ({type: 'working_lists', action: 'cancel'}, {}, reload_page)
    }
*/    
    

    $_DO.choose_tab_working_plan_common = function (e) {
    
        var name = e.tab.id
                        
        var layout = w2ui ['passport_layout']

        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('working_plan_common.active_tab', name)

        if (/^working_plan_common_plan_/.test (name)) {
            name = 'working_plan_common_plan'
        }

        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item
        
        data.active_tab = localStorage.getItem ('working_plan_common.active_tab') || 'working_plan_common_items'

        data.__read_only = 1
       
        it.gis_status_label = data.vc_gis_status [it.id_ctr_status]

        done (data)
        
    }

})