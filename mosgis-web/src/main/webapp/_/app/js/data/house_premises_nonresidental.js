define ([], function () {

    var grid_name = 'house_premises_nonresidental_grid'

    $_DO.create_house_premises_nonresidental = function (e) {
        $_SESSION.set ('record', {iscommonproperty: 0})
        use.block ('premise_nonresidental_popup')
    }

    $_DO.edit_house_premises_nonresidental = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        $_SESSION.set ('record', w2ui [e.target].get (e.recid))
        use.block ('premise_nonresidental_popup')
    }
    
    $_DO.delete_house_premises_nonresidental = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = w2ui[grid_name]

        if (!confirm ('Удалить запись?')) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
                
        query ({
        
            type:   'premises_nonresidental', 
            id:     grid.getSelection () [0].recid,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    

    $_DO.patch_house_premises_nonresidental = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = w2ui[grid_name]
    
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
            die ('foo', 'Помещение с таким номером уже зарегистрировано')
        })
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'premises_nonresidental', action: 'update', id: e.recid}

        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
            
            grid.unlock ()

            grid.reload ()

        }, edit_failed (grid, e))
        
    }

    return function (done) {        
        
        var layout = w2ui ['house_premises_layout']
            
        if (layout) layout.unlock ('main')
        
        get_nsi ([17, 254, 253, 261], done)
        
    }
    
})