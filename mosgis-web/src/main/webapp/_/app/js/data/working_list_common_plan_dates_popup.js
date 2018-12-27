define ([], function () {

    $_DO.toggle_working_list_common_plan_dates_popup = function (e) {
    
darn (e.target.textContent)
    
    }

    $_DO.update_working_list_common_plan_dates_popup = function (e) {
/*
        var f = w2ui ['working_list_common_plan_dates_popup_form']

        var v = f.values ()
        
        if (!v.reasonofannulment) die ('reasonofannulment', 'Укажите, пожалуйста, причину аннулирования')
        if (v.reasonofannulment.length > 1000) die ('reasonofannulment', 'Максимальная допустимая длина — 1000 символов')

        query ({type: 'charters', action: 'annul'}, {data: v}, reload_page)
*/            
    }

    return function (done) {

        var data = $_SESSION.delete ('cell')

        var grid = w2ui ['working_list_common_plan_grid']

        var r = grid.get (data.uuid)
        
        data.label = r ['w.label']

        done (data)

    }
    
})