define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['municipal_services_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'municipal_services', 
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

    $_DO.delete_municipal_services = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_municipal_services = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_municipal_services = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('municipal_service_popup')
    }

    $_DO.edit_municipal_services = function (e) {       

        var grid = w2ui ['municipal_services_grid']

        var r = grid.get (grid.getSelection () [0])
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')
        
        $_SESSION.set ('record', r)

        use.block ('municipal_service_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'municipal_services', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})