define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['org_works_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'org_works', 
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

    $_DO.delete_org_works = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_org_works = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_org_works = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('org_work_popup')
    }

    $_DO.edit_org_works = function (e) {       

        var grid = w2ui ['org_works_grid']

        var r = grid.get (grid.getSelection () [0])
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')
        
        $_SESSION.set ('record', r)

        use.block ('org_work_popup')

    }

    return function (done) {
        
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'org_works', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})