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
        
        var dt = new Date (data.item.startdate + 'Z')

        function dtIso      () {return dt.toISOString ().substr (0, 10)}
        function dtIncMonth () {dt.setMonth (dt.getMonth () + 1)}
        
        var ms_to = new Date (data.item.enddate + 'Z').getTime ()
        
        dt.setDate (1)
        
        data.begins = []
        data.ends   = []

        while (dt.getTime () <= ms_to) {
        
            var label = w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear ()
        
            data.begins.push ({id: dtIso (), text: label})

            dtIncMonth ()
            dt.setDate (0)

            data.ends.unshift ({id: dtIso (), text: label})

            dt.setDate (1)
            dtIncMonth ()
            
            if (dt.getTime () > ms_to) break
                    
        }
                
        data.record = {
            dt_from: data.begins [0].id,
            dt_to: data.ends [0].id,
        }
        
        done (data)

    }

})