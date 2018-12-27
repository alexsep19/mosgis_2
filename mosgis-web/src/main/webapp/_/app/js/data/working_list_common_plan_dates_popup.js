define ([], function () {

    $_DO.toggle_working_list_common_plan_dates_popup = function (e) {
    
        var $td = $(e.target)
        
        $td.toggleClass ('on')

    }

    $_DO.clear_working_list_common_plan_dates_popup = function (e) {
        $('table.cal td.local').removeClass ('on')
    }
    
    $_DO.update_working_list_common_plan_dates_popup = function (e) {

        var days_bitmask = 0

        var $list = $('table.cal td.on')

        var v = {cnt: $list.length, days_bitmask: 0}

        if (!v.cnt) return w2popup.close ()

        $list.each (function () {v.days_bitmask |= (1 << (this.textContent - 1))})

        var form = w2ui ['working_list_common_plan_dates_popup_form']

        var data = form.record

        var grid = w2ui ['working_list_common_plan_grid']

        var r = {}; r ['cnt_' + (data.month + 1)] = v.cnt

        grid.set (data.uuid, r)

        w2popup.close ()

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
                    date:  dt.getDate  (), 
                    class: dt.getMonth () == data.month ? 'local' : 'alien'
                })

                dt.setDate (dt.getDate () + 1)

            }                            

        }     
        
        done (data)

    }
    
})