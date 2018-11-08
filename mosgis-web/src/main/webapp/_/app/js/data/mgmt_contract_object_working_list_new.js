define ([], function () {

    $_DO.update_mgmt_contract_object_working_list_new = function (e) {

        var form = w2ui ['working_list_form']

        var v = form.values ()
        
        if (v.dt_from > v.dt_to) die ('dt_to', 'Окончание периода перечня не может быть раньше начала. Укажите корректные значения.')
        
        v.uuid_contract_object = $_REQUEST.id
        
        query ({type: 'working_lists', id: undefined, action: 'create'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            var grid = w2ui ['working_lists_grid']

            grid.reload (grid.refresh)
            
            if (data.id) w2confirm ('Перечень создан. Перейти к его редактированию?').yes (function () {openTab ('/working_list/' + data.id) })

        })       
        
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
                
        data.begins = data.periods.map (function (i) {return {
            id: i.id.substr (0, 8) + '01',
            text: i.text
        }})
        
        data.ends   = clone (data.periods).reverse ()

        data.record = {
            dt_from: data.begins [0].id,
            dt_to:   data.ends [0].id,
        }

        done (data)

    }

})