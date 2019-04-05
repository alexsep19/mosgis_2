define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['base_decision_msps_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'base_decision_msps', 
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

    $_DO.import_base_decision_msps = function (e) {

        if (!confirm('Импортировать справочник?'))
            return

        var grid = w2ui ['base_decision_msps_grid']

        grid.lock()
        $_SESSION.set('base_decision_msps_importing', 1)

        query({type: 'base_decision_msps', id: null, action: 'import'}, {}, $_DO.check_base_decision_msps)

    }

    $_DO.check_base_decision_msps = function () {

        var grid = w2ui ['base_decision_msps_grid']

        query ({type: 'base_decision_msps', id: null, part: 'log'}, {}, function (d) {
        
            var is_importing = $_SESSION.get ('base_decision_msps_importing')

            if (!d.log.uuid) return is_importing ? null : $_DO.import_base_decision_msps ()
        
            if (d.log.is_over) {            
                $_SESSION.delete ('base_decision_msps_importing')
                if (is_importing && grid) use.block ('base_decision_msps')
                return
            }
                        
            setTimeout (function () {w2ui ['base_decision_msps_grid'].lock ('Импорт данных...', 1)}, 10)

            setTimeout ($_DO.check_base_decision_msps, 2000)

        })

    }

    $_DO.delete_base_decision_msps = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_base_decision_msps = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_base_decision_msps = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('base_decision_msp_popup')
    }

    $_DO.edit_base_decision_msps = function (e) {       

        var grid = w2ui [e.target]

        var r = grid.get (e.recid)
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        $_SESSION.set ('record', r)

        use.block ('base_decision_msp_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'base_decision_msps', part: 'vocs', id: undefined}, {}, function (data) {

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