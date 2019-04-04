define ([], function () {

    var grid_name = 'house_passport_entrances_grid'

    function getGrid () {
        return w2ui [grid_name]
    }

    function getId () {
        return getGrid ().getSelection () [0]
    }

    function getTia () {

        return {
            type: 'entrances',
            id: getId (),
            action: 'update',
        }

    }

    $_DO.edit_house_passport_entrances = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        if (getGrid ().get(getId ()).terminationdate) die ('foo', 'Редактирование аннулированных записей запрещено.')

        use.block ('entrance_popup')
        
    }

    $_DO.annul_house_passport_entrances = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        $_SESSION.set ('tia', getTia ())
    
        use.block ('annul_popup')        
    
    }

    $_DO.restore_house_passport_entrances = function (e) {

        check_rights(grid_name, e, is_own_srca_r)

        if (!confirm ('Отменить аннулирование записи?')) return

        query ({type: 'entrances', id: getId (), action: 'restore'}, {}, reload_page)

    }

    $_DO.delete_house_passport_entrances = function (e) {
    
        check_rights(grid_name, e, is_own_srca_r)

        var grid = w2ui[grid_name]
        var id   = grid.getSelection() [0]
        var r    = grid.get (id)

        if (!confirm ('Удалить запись?')) return 

        for (code in $('body').data ('data').vc_nsi_192) {
        
            if (code == 'items') continue
            
            if (r ['cnt_' + code] > 0) {                
                e.preventDefault ()
                return alert ('Пока к подъезду относится хотя бы один лифт, удаление невозможно')                
            }

        }

        $('.w2ui-message').remove ()

        e.preventDefault ()

        query ({

            type:   'entrances', 
            id:     id,
            action: 'delete',

        }, {}, reload_page)

    }
    
    $_DO.patch_house_passport_entrances = function (e) {
    
        check_rights(grid_name, e, is_own_srca_r)

        var grid = this

        var col = grid.columns [e.column]
        
        if (col._is_lift) {
        
            var old = e.value_original; if (!old) old = 0
            var delta = parseInt (e.value_new) - parseInt (old)

            if (delta < 0) {
                alert ('Удалять лифты можно только на соответствующей вкладке')
                return e.preventDefault ()                
            }
            
            if (delta > 0) {
                
                var suffix = 'ов'
                
                if (delta < 11 || delta > 20) {
                    var mod = delta % 10
                    if (mod && mod < 5) suffix = mod == 1 ? '' : 'а'
                }
                
                query ({type: 'lifts', action: 'add', id: undefined}, {data: {                

                    uuid_entrance: e.recid,
                    code_vc_nsi_192: col.field.substr (4),
                    cnt: delta,

                }}, function () {

                    alert ('Добавлен' + (delta > 1 ? 'о ' : ' ') + delta + ' лифт' + suffix + '. Для отправки сведений в ГИС ЖКХ необходимо перейти на вкладку "Лифты", с целью заполнения всех обязательных полей.')

                    grid.unlock ()

                    grid.reload ()

                })
                
            }
            
            return

        }           

        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var d = {}; d [data.k] = data.v

        query ({type: 'entrances', action: 'update', id: e.recid}, {data: d}, function () {
            
            grid.unlock ()
            
            grid.reload ()

        }, edit_failed (grid, e))
        
    }

    return function (done) {        
        
        var layout = w2ui ['passport_layout']
            
        if (layout) layout.unlock ('main')

        get_nsi ([330], done)

    }
    
})