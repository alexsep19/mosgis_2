define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['vc_persons_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'vc_persons', 
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

    $_DO.delete_vc_persons = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_vc_persons = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_vc_persons = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('vc_person_new')
    }

    $_DO.edit_vc_persons = function (e) {       

        var grid = w2ui ['vc_persons_grid']

        var r = grid.get (grid.getSelection () [0])
        
        $_SESSION.set ('record', r)

        use.block ('vc_person_popup')

    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'vc_persons', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})