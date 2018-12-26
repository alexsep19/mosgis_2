define ([], function () {

    $_DO.patch_working_list_common_plan = function (e) {
/*    
        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'working_list_items', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
        
            query ({type: 'working_list_items', id: undefined}, {data: {uuid_working_list: $_REQUEST.id}}, function (d) {
            
                var totalcost

                $.each (d.tb_work_list_items, function () {
                
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
*/        
    
    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        data.uuid_plan = $_SESSION.delete ('uuid_plan')

        query ({type: 'working_list_items', id: undefined}, {data: {uuid_working_list: $_REQUEST.id}}, function (d) {
        
            data.records = dia2w2uiRecords (d.tb_work_list_items)
        
            done (data)
        
        })
                
    }

})