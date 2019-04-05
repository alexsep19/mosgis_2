define ([], function () {

    var grid_name = 'overhaul_regional_program_house_works_grid'

    $_DO.approve_overhaul_regional_program_house_works = function (e) {

        if (!confirm ('Опубликовать эти данные в ГИС ЖКХ?')) return

        var data = clone ($('body').data ('data'))

        query ({

            type: 'overhaul_regional_program_house_works',
            id: data.item.program_uuid,
            action: 'approve',

        }, {}, reload_page)

    }

    $_DO.create_overhaul_regional_program_house_works = function (e) {
            
        $_SESSION.set ('record', {})
        use.block ('overhaul_regional_program_house_works_popup')
    
    }

    $_DO.edit_overhaul_regional_program_house_works = function (e) {
        
        var g = w2ui [grid_name]
        $_SESSION.set ('record', g.get (g.getSelection ()[0]))
        use.block ('overhaul_regional_program_house_works_popup')

    }

    $_DO.delete_overhaul_regional_program_house_works = function (e) {

        var grid = w2ui [grid_name]
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        
        query ({
        
            type:   'overhaul_regional_program_house_works', 
            id:     grid.getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})