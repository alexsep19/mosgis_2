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
        
        data.rows = []
        
        var dt = new Date (data.year, data.month, 1)
        dt.setDate (dt.getDate () - (dt.getDay () + 6) % 7)

        for (var i = 0; i < 5; i ++) {
        
            var row = {cells: []}
            
            data.rows.push (row)

            for (var j = 0; j < 7; j ++) {

                row.cells.push ({
                    date: dt.getDate (), 
                    class: dt.getMonth () == data.month ? 'local' : 'alien'
                })

                dt.setDate (dt.getDate () + 1)

            }                            

        }     
        
        done (data)

    }
    
})