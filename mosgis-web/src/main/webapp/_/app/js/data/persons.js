define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['persons_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'persons', 
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

    $_DO.delete_persons = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_persons = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_persons = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('municipal_service_popup')
    }

    $_DO.edit_persons = function (e) {       

        var grid = w2ui ['persons_grid']

        var r = grid.get (grid.getSelection () [0])
        
        $_SESSION.set ('record', r)

        use.block ('person_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'persons', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})