define ([], function () {

    $_DO.create_house_premises_residental = function (e) {
        $_SESSION.set ('record', {uuid_entrance: -1})
        use.block ('premise_residental_popup')
    }

    $_DO.edit_house_premises_residental = function (e) {
        $_SESSION.set ('record', w2ui [e.target].get (e.recid))
        use.block ('premise_residental_popup')
    }
    
    $_DO.delete_house_premises_residental = function (e) {
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
                
        query ({
        
            type:   'premises_residental', 
            id:     w2ui [e.target].getSelection () [0].recid,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    

    $_DO.patch_house_premises_residental = function (e) {
        
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
            die ('foo', 'Помещение с таким номером уже зарегистрировано')
        })
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'premises_residental', action: 'update', id: e.recid}

        var d = {}; d [data.k] = data.v || null

        query (tia, {data: d}, function () {
            
            grid.unlock ()
            
            grid.reload ()

        }, edit_failed (grid, e))
        
    }

    return function (done) {        
        
        var layout = w2ui ['house_premises_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'entrances', id: undefined}, {data: {"uuid_house": $_REQUEST.id}, cmd: "get", selected: [], limit:100, offset: 0}, function (d) {

            var k = 'entrances'

            var h = {}; h [k] = d
                .tb_entrances
                .map (function (r) {return {
                    id: r.id, 
                    label: r.entrancenum,
                    fake: r.is_annuled
                }})

            add_vocabularies (h, h)
            
            h.entrances [''] = 'Отдельный вход'

            $('body').data ('data') [k] = h [k]
            
            get_nsi ([14, 30, 273, 258, 259, 254, 253, 261], done)
        
        })        
        
    }
    
})