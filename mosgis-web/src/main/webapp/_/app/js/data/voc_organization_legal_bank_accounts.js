define ([], function () {

    $_DO.create_voc_organization_legal_bank_accounts = function () {

        use.block ('bank_account_popup')

    }
    
    $_DO.edit_voc_organization_legal_bank_accounts = function (e) {
    
        var grid = w2ui ['voc_organization_legal_bank_accounts_grid']
        
        $_SESSION.set ('record', grid.get (e.recid))

        use.block ('bank_account_popup')

    }

    return function (done) {

        w2ui ['voc_organization_legal_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data);
        
    }

})