define ([], function () {

    var form_name = 'overhaul_address_program_houses_new_form'
    var grid_name = 'overhaul_address_program_houses_grid'

    $_DO.open_houses_popup = function (e) {

        var f = w2ui [form_name]
        var g = w2ui [grid_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }

        function done () {

            $('body').data ('data', saved.data)

            $_SESSION.set ('record', saved.record)

            use.block ('overhaul_address_program_houses_new')

        }

        $('body').data ('houses_popup.post_data', {search:[
            {field: 'is_condo', operator: 'is', value: 1}
        ], searchLogic: "AND"})
    
        $('body').data ('houses_popup.callback', function (r) {

            if (!r) return done ()

            saved.record.house = r.id
            saved.record.house_address = r.address

            done ()

        })

        use.block ('houses_popup')

    }

    $_DO.update_overhaul_address_program_houses_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        delete v.house_address
        
        if (!v.house) die ('house_address', 'Пожалуйста, укажите адрес дома')
        
        v.program_uuid = $_REQUEST.id

        var tia = {type: 'overhaul_address_program_houses'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui [grid_name]
        
        var data = clone ($('body').data ('data'))

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу дома и видов работ?').yes (function () {openTab ('/overhaul_address_program_house/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})