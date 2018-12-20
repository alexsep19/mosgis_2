define ([], function () {

    function getGrid () {
        return w2ui ['house_passport_lifts_grid']
    }
    
    function getId () {
        return getGrid ().getSelection () [0]
    }

    function getTia () {

        return {
            type: 'lifts',
            id: getId (),
            action: 'update',
        }

    }
    
    $_DO.edit_house_passport_lifts = function (e) {

        if (getGrid ().get (getId ()).terminationdate) die ('foo', 'Редактирование аннулированных записей запрещено.')
        
        use.block ('lift_popup')
        
    }

    $_DO.annul_house_passport_lifts = function (e) {
    
        $_SESSION.set ('tia', getTia ())
    
        use.block ('annul_popup')        
    
    }

    $_DO.restore_house_passport_lifts = function (e) {

        if (!confirm ('Отменить аннулирование записи?')) return

        query ({type: 'lifts', id: getId (), action: 'restore'}, {data: {        

            terminationdate: null,
            annulmentinfo: null,
            code_vc_nsi_330: null,

        }}, reload_page)

    }
    
    $_DO.delete_house_passport_lifts = function (e) {
    
        var grid = getGrid ()
        var id   = getId ()
        var r    = grid.get (id)

        if (!confirm ('Удалить запись?')) return 
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'lifts', 
            id:     id,
            action: 'delete',
            
        }, {}, reload_page)
        
    }
    
    $_DO.patch_house_passport_lifts = function (e) {
        
        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }

        if (data.k == 'factorynum') $.each (grid.records, function () {        
            if (this.terminationdate) return;
            if (this.id == e.recid) return;
            if (this.factorynum != data.v) return;
            e.preventDefault ()
            die ('foo', 'Лифт с таким заводским номером уже зарегистрирован')
        })
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'lifts', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v        

        query (tia, {data: d}, function () {
            
            grid.unlock ()
            
            grid.reload ()

        })
        
    }

    return function (done) {        
        
        var layout = w2ui ['passport_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'entrances', id: undefined}, {data: {"uuid_house": $_REQUEST.id}, cmd: "get", selected: [], limit:100, offset: 0}, function (d) {

            var k = 'entrances'

            var h = {}; h [k] = d
                .tb_entrances
                .map    (function (r) {return {
                    id: r.id, 
                    label: r.entrancenum,
                    fake: r.is_annuled
                }})

            add_vocabularies (h, h)

            $('body').data ('data') [k] = h [k]

            get_nsi ([330], done)
        
        })        
        
    }
    
})