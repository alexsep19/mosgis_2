define ([], function () {

    $_DO.update_agreement_payment_annul_popup = function (e) {

        var f = w2ui ['agreement_payment_annul_popup_form']

        var v = f.values ()
        
        if (!v.reasonofannulment) die ('reasonofannulment', 'Укажите, пожалуйста, причину аннулирования')
        if (v.reasonofannulment.length > 1000) die ('reasonofannulment', 'Максимальная допустимая длина — 1000 символов')
        
        var grid = w2ui ['public_property_contract_agreement_payments_grid']

        query ({type: 'agreement_payments', action: 'annul', id: grid.getSelection () [0]}, {data: v}, function (d) {
            w2popup.close ()
            grid.reload (grid.refresh)            
        })

    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})