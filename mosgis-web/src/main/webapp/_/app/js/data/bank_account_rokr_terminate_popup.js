define ([], function () {

    $_DO.update_bank_account_rokr_terminate_popup = function (e) {

        var f = w2ui ['bank_account_rokr_terminate_popup_form']

        var v = f.values ()
        
        if (!v.closedate) die ('closedate', 'Укажите, пожалуйста, дату закрытия')
        
        query ({type: 'bank_accounts', action: 'terminate'}, {data: v}, reload_page)
                    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})