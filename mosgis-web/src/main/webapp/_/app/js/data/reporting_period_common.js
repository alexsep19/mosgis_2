define ([], function () {

    var form_name = 'reporting_period_common_form'    
/*    
    $_DO.approve_reporting_period_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'reporting_periods', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.alter_reporting_period_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'reporting_periods', action: 'alter'}, {}, reload_page)
    }
*/
    $_DO.choose_tab_reporting_period_common = function (e) {
    
        var name = e.tab.id
                        
        var layout = w2ui ['passport_layout']

        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('reporting_period_common.active_tab', name)

        if (/^reporting_period_common_plan_/.test (name)) {
            name = 'reporting_period_common_plan'
        }

        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item
        
        data.active_tab = localStorage.getItem ('reporting_period_common.active_tab') || 'reporting_period_planned_works'

        data.__read_only = 1
       
        it.gis_status_label = data.vc_gis_status [it.id_ctr_status]

        done (data)
        
    }

})