define ([], function () {

    $_DO.fill_reporting_period_planned_works = function (e) {
    
        if (!confirm ('Заполнить отчёт плановыми показателями?')) return
    
        var grid = w2ui ['reporting_period_planned_works_grid']
        
        grid.lock ()

        query ({type: 'reporting_periods', action: 'fill'}, {}, reload_page)
    
    }

    $_DO.patch_reporting_period_planned_works = function (e) {
    
        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'working_plan_items', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
        
            query ({type: 'working_plan_items', id: undefined}, {data: {uuid_working_plan: $('body').data ('data').item.uuid_working_plan}}, function (d) {
            
                var totalcost

                $.each (d.tb_work_plan_items, function () {
                
                    if (this.uuid == e.recid) totalcost = this.totalcost
                
                })
                
                $.each (grid.records, function () {            
                
                    if (this.uuid == e.recid) {
                        this [data.k] = data.v
                        this.totalcost = totalcost
                    }
                    
                    delete this.w2ui
                    
                })

                grid.unlock ()                    

                grid.refresh ()

            })                

        }, edit_failed (grid, e))
    
    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = $('body').data ('data')

        query ({type: 'working_plan_items', id: undefined}, {data: {uuid_working_plan: data.item.uuid_working_plan}}, function (d) {

            $.each (d.tb_work_plan_items, function () {
                this._plan_cost = this ['li.price'] * this ['li.amount'] * this ['workcount']
            })

            data.records = dia2w2uiRecords (d.tb_work_plan_items)

            done (clone (data))

        })
                
    }

})