define ([], function () {

    $_DO.create_public_property_contract_agreement_payments = function (e) {
        use.block ('agreement_payment_popup')
    }
    
    $_DO.delete_public_property_contract_agreement_payments = function (e) {
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()

        query ({
        
            type:   'agreement_payments', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
        
    }

    return function (done) {
        w2ui ['topmost_layout'].unlock ('main')               
        var data = clone ($('body').data ('data'))
        done (data)
    }

})