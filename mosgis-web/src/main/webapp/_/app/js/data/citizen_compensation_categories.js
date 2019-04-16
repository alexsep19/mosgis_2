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

        use.block ('citizen_compensation_categories_import')
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