define ([], function () {

    $_DO.update_supply_resource_contract_terminate_popup = function (e) {

        var f = w2ui ['supply_resource_contract_terminate_popup_form']

        var v = f.values ()
        var it = $('body').data ('data').item
        
        if (!v.terminate) die ('terminate', 'Укажите, пожалуйста, дату прекращения действия договора')
        if (v.terminate < it.effectivedate) die ('terminate', 'Дата расторжения не может предшествовать дате вступления в силу договора')
        
        if (!v.code_vc_nsi_54) die ('code_vc_nsi_54', 'Укажите, пожалуйста, причину прекращения действия договора')

        query ({type: 'supply_resource_contracts', action: 'terminate'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
    }
    
})