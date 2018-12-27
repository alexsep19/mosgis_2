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

        var v = {workcount: $list.length, days_bitmask: 0}

        if (!v.workcount) return w2popup.close ()

        $list.each (function () {v.days_bitmask |= (1 << (this.textContent - 1))})

        var form = w2ui ['working_list_common_plan_dates_popup_form']

        var data = form.record

        v.uuid_working_list_item = data.uuid_working_list_item
        v.month = data.month + 1

        w2popup.lock ('')

        query ({type: 'working_plans', id: data.uuid, action: 'update'}, {data: v}, function () {
            
            var grid = w2ui ['working_list_common_plan_grid']
            
            var r = {}; 
            r ['cnt_' + (data.month + 1)] = v.workcount
            r ['days_bitmask_' + (data.month + 1)] = v.days_bitmask.toString (16);

            grid.set (data.uuid_working_list_item, r)
            
            w2popup.close ()
            
        })
        
    }

    return function (done) {

        var data = $_SESSION.delete ('cell')

        var grid = w2ui ['working_list_common_plan_grid']

        var r = grid.get (data.uuid_working_list_item)
        
        data.label = r ['w.label']
        
        data.rows = []
        
        var dt = new Date (data.year, data.month, 1)
        
        dt.setDate (dt.getDate () - (dt.getDay () + 6) % 7)
        
        var days = []

        for (var i = 0; i < 5; i ++) {
        
            var row = {cells: []}
            
            data.rows.push (row)

            for (var j = 0; j < 7; j ++) {
            
                var cell = {
                    date:  dt.getDate  (), 
                    class: dt.getMonth () == data.month ? 'local' : 'alien'
                }

                row.cells.push (cell)
                if (cell.class == 'local') days.push (cell)

                dt.setDate (dt.getDate () + 1)

            }                            

        }
        
        var m = r ['days_bitmask_' + (data.month + 1)]

        if (m) {
        
            m = parseInt (m, 16)

            $.each (days, function () {

                if (m & (1 << (this.date - 1))) this.class += ' on'
            
            })

        }

        done (data)

    }
    
})