define ([], function () {

    $_DO.create_interval_objects = function (e) {

        use.block ('interval_object_popup')

    }
    
    $_DO.edit_interval_objects = function (e) {
    
        $_SESSION.set ('record', w2ui ['interval_objects_grid'].get (e.recid))

        use.block ('interval_object_popup')

    }

    $_DO.delete_interval_objects = function (e) {

        if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

        var grid = w2ui ['interval_objects_grid']

        query ({type: 'interval_objects', id: grid.getSelection () [0], action: 'delete'}, {}, function (d) {
            grid.reload (grid.refresh)
        })

    }

    return function (done) {        

        var layout = w2ui ['topmost_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data)
                
    }

})