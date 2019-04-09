define ([], function () {

    var grid_name = 'house_passport_blocks_grid'

    $_DO.create_house_passport_blocks = function (e) {
        $_SESSION.set ('record', {is_nrs: "0", code_vc_nsi_30: "1"})
        use.block ('block_popup')
    }
    
    $_DO.delete_house_passport_blocks = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        if (!confirm ('Удалить запись?')) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
                
        query ({
        
            type:   'blocks', 
            id:     grid.getSelection () [0].recid,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    

    $_DO.patch_house_passport_blocks = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }

        if (data.k == 'premisesnum') $.each (grid.records, function () {        
            if (this.terminationdate) return;
            if (this.id == e.recid) return;
            if (this.premisesnum != data.v) return;
            e.preventDefault ()
            die ('foo', 'Блок с таким номером уже зарегистрирован')
        })
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'blocks', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
            
            grid.unlock ()
            
            grid.reload ()

        }, edit_failed (grid, e))
        
    }

    return function (done) {        
        
        var layout = w2ui ['passport_layout']
            
        if (layout) layout.unlock ('main')
        
        var data = $('body').data ('data')
        
        data.vc_is_nrs = [
            {id: "0", label: "Жилое помещение"},
            {id: "1", label: "Нежилое помещение"},
        ]        
        
        add_vocabularies (data, {vc_is_nrs: 1})

        $('body').data ('data', data)
        
        get_nsi ([17, 14, 30, 254, 253, 261, 259, 258], done)
        
    }
    
})