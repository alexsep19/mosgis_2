define ([], function () {

    $_DO.update_rc_contract_annul_popup = function (e) {

        var f = w2ui ['rc_contract_annul_popup_form']

        var v = f.values ()
        
        if (!v.reason_of_annulment) die ('reason_of_annulment', 'Укажите, пожалуйста, причину аннулирования')
        
        query ({type: 'rc_contracts', action: 'annul'}, {data: v}, reload_page)
                    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})