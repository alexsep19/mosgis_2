define ([], function () {

    var grid_name = 'block_common_living_rooms_grid'

    $_DO.create_block_common_living_rooms = function (e) {
    
        if ($('body').data ('data').item.is_annuled) die ('foo', 'Создание вложенных объектов запрещено, так как данная запись аннулирована')
    
        $_SESSION.set ('record', {})
        
        use.block ('living_room_popup')
        
    }
    
    $_DO.delete_block_common_living_rooms = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = w2ui['block_common_living_rooms_grid']

        if (!confirm ('Удалить запись?')) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
                
        query ({
        
            type:   'living_rooms', 
            id:     grid.getSelection () [0].recid,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    

    $_DO.patch_block_common_living_rooms = function (e) {

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
        
        var tia = {type: 'living_rooms', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
            
            grid.unlock ()
            
            grid.reload ()

        }, edit_failed (grid, e))
        
    }

    return function (done) {        
        
        w2ui ['passport_layout'].unlock ('main')
        
        var data = $('body').data ('data')

        var block = data.item
        
        data.vc_blocks = [
            {id: block.uuid, label: block.blocknum},
        ]        
        
        add_vocabularies (data, {vc_blocks: 1})

        $('body').data ('data', data)
        
        get_nsi ([261, 273], done)
        
    }
    
})