define ([], function () {

    $_DO.create_voc_organization_legal_bank_accounts = function () {

        use.block ('bank_account_popup')

    }
    
    $_DO.edit_voc_organization_legal_bank_accounts = function (e) {
    
        var grid = w2ui ['voc_organization_legal_bank_accounts_grid']
        
        $_SESSION.set ('record', grid.get (e.recid))

        use.block ('bank_account_popup')

    }

    $_DO.delete_voc_organization_legal_bank_accounts = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'bank_accounts', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['voc_organization_legal_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data);
        
    }

})