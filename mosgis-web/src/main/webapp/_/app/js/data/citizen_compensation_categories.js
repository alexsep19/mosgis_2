define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['citizen_compensation_categories_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'citizen_compensation_categories', 
            id:     id,
            action: action
        }

        query (tia, {}, function () {
            grid.reload (function () {
                grid.refresh ()            
                grid.selectNone ()
                grid.select (id)
            })
        })

    }

    $_DO.import_citizen_compensation_categories = function (e) {

        if (!confirm('Импортировать справочник?'))
            return

        var grid = w2ui ['citizen_compensation_categories_grid']

        grid.lock()
        $_SESSION.set('citizen_compensation_categories_importing', 1)

        query({type: 'citizen_compensation_categories', id: null, action: 'import'}, {}, $_DO.check_citizen_compensation_categories)

    }

    $_DO.check_citizen_compensation_categories = function () {

        var grid = w2ui ['citizen_compensation_categories_grid']

        query ({type: 'citizen_compensation_categories', id: null, part: 'log'}, {}, function (d) {
        
            var is_importing = $_SESSION.get ('citizen_compensation_categories_importing')

            if (!d.log.uuid) return is_importing ? null : $_DO.import_citizen_compensation_categories ()
        
            if (d.log.is_over) {            
                $_SESSION.delete ('citizen_compensation_categories_importing')
                if (is_importing && grid) use.block ('citizen_compensation_categories')
                return
            }
                        
            setTimeout (function () {w2ui ['citizen_compensation_categories_grid'].lock ('Запрос в ГИС ЖКХ...', 1)}, 10)

            setTimeout ($_DO.check_citizen_compensation_categories, 5000)

        })

    }

    $_DO.delete_citizen_compensation_categories = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_citizen_compensation_categories = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_citizen_compensation_categories = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('citizen_compensation_category_popup')
    }

    $_DO.edit_citizen_compensation_categories = function (e) {       

        var grid = w2ui [e.target]

        var r = grid.get (e.recid)
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        $_SESSION.set ('record', r)

        use.block ('citizen_compensation_category_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'citizen_compensation_categories', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)
            data._can = {
                create: $_USER.has_nsi_20(9, 10)
            }
            data._can.delete = data._can.edit = data._can.create

            done (data)

        }) 

    }

})