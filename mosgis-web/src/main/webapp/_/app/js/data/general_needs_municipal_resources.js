define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['general_needs_municipal_resources_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'general_needs_municipal_resources', 
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

    $_DO.delete_general_needs_municipal_resources = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_general_needs_municipal_resources = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_general_needs_municipal_resources = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('general_needs_municipal_resource_popup')
    }

    $_DO.edit_general_needs_municipal_resources = function (e) {       

        var grid = w2ui [e.target]

        var r = grid.get (e.recid)
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        $_SESSION.set ('record', r)

        use.block ('general_needs_municipal_resource_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'general_needs_municipal_resources', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})