define ([], function () {

    $_DO.update_rc_contract_terminate_popup = function (e) {

        var f = w2ui ['rc_contract_terminate_popup_form']

        var v = f.values ()
        
        if (!v.date_of_termination) die ('date_of_termination', 'Укажите, пожалуйста, дату расторжения')
        if (v.date_of_termination > new Date ().toISOString ()) die ('date_of_termination', 'Дата расторжения договора не может находиться в будущем')
        if (!v.reason_of_termination) die ('reason_of_termination', 'Укажите, пожалуйста, причину расторжения')
        
        query ({type: 'rc_contracts', action: 'terminate'}, {data: v}, reload_page)
                    
    }

    return function (done) {
    
        query ({type: 'add_services', id: undefined}, {limit: 100000, offset: 0}, function (d) {
        
            data.reasons_of_termination = d.root.map (function (r) {return {
                id: r.uuid,
                text: r.label
            }})

            done (data)

        })
        
        done (clone ($('body').data ('data')))
        
    }
    
})