define ([], function () {

    $_DO.create_voc_organization_legal_bank_accounts = function () {

        use.block ('bank_account_popup')

    }

    return function (done) {

        w2ui ['voc_organization_legal_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data);
        
    }

})