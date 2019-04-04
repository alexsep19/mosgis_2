define ([], function () {


    var grid_name = 'house_living_rooms_grid'

    $_DO.create_house_living_rooms = function (e) {
    
        var data = $('body').data ('data')

        if (data.item.hasblocks && !data.vc_blocks.items.length) die ('foo', 'Данный жилой дом относится к блокированной застройке, однако сейчас не зарегистрировано ни одного его блока. Операция отменена.')
    
        $_SESSION.set ('record', {})
        
        use.block ('living_room_popup')
        
    }
    
    $_DO.delete_house_living_rooms = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = w2ui['house_living_rooms_grid']

        if (!confirm ('Удалить запись?')) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
                
        query ({
        
            type:   'living_rooms', 
            id:     grid.getSelection () [0].recid,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    

    $_DO.patch_house_living_rooms = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }
        
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
        
        w2ui ['topmost_layout'].unlock ('main')
                    
        var data = $('body').data ('data')
        
        function go () {
            get_nsi ([261, 273], done)
        }
        
        if (data.item.is_condo) {
        
            query ({type: 'premises_residental', id: undefined}, {data: {uuid_house: $_REQUEST.id}}, function (d) {

                var dd = {vc_premises: d

                    .tb_premises_res                    

                    .map (function (r) {return {
                        id: r.uuid, 
                        label: r.premisesnum,
                        fake: r.code_vc_nsi_30 != '2' || r.is_deleted || r.is_annuled
                    }})

                }

                add_vocabularies (dd, dd)

                for (i in dd) data [i] = dd [i]

                go ()

            })
        
        }
        else {
        
            query ({type: 'blocks', id: undefined}, {data: {uuid_house: $_REQUEST.id}}, function (d) {

                var dd = {vc_blocks: d

                    .tb_blocks

                    .map (function (r) {return {
                        id: r.uuid, 
                        label: r.blocknum,
                        fake: r.is_nrs || r.is_deleted || r.is_annuled
                    }})

                }

                add_vocabularies (dd, dd)

                for (i in dd) data [i] = dd [i]

                go ()

            })
        
        }        

    }

})