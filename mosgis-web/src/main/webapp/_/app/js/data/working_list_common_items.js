define ([], function () {

    $_DO.patch_working_list_common_items = function (e) {
    
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
        
            $.each (grid.records, function () {            
                if (this.uuid == e.recid) this [data.k] = data.v
                delete this.w2ui
            })
            
            grid.unlock ()                    
                        
            grid.refresh ()

        }, edit_failed (grid, e))
    
    }

    $_DO.delete_working_list_common_items = function (e) {
    
        if (!e.force) return
        
        var grid = w2ui [e.target]
        
        grid.lock ()
        
        query ({type: 'working_list_items', id: grid.getSelection () [0], action: 'delete'}, {}, function () {
        
            use.block ('working_list_common_items')
            
        })
        
    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = $('body').data ('data')

        query ({type: 'working_list_items', id: undefined}, {data: {uuid_working_list: $_REQUEST.id}}, function (d) {
        
            data.records = dia2w2uiRecords (d.tb_work_list_items)
        
            done (clone (data))
        
        })
                
    }

})