define ([], function () {

    $_DO.create_voc_organization_legal_bank_accounts = function () {

        use.block ('bank_account_popup')

    }
    
    $_DO.edit_voc_organization_legal_bank_accounts = function (e) {
    
        var grid = w2ui ['voc_organization_legal_bank_accounts_grid']
        
        var r = grid.get (e.recid)
        
        if (r.uuid_org != $_REQUEST.id) die ('foo', 'Владельцем данного счёта является ' + r ['org.label'] + '. Он указан как платёжный реквизит ' + $('body').data ('data').item.label + ' на основании договора услуг РЦ. Редактирование отменено.')

        openTab('/bank_account/' + e.recid)
    }

    $_DO.delete_voc_organization_legal_bank_accounts = function (e) {    
    
        var id = w2ui [e.target].getSelection () [0]
    
        var grid = w2ui ['voc_organization_legal_bank_accounts_grid']
        
        var r = grid.get (id)

        if (!e.force) {
        
            if (r.uuid_org != $_REQUEST.id) die ('foo', 'Владельцем данного счёта является ' + r ['org.label'] + '. Он указан как платёжный реквизит ' + $('body').data ('data').item.label + ' на основании договора услуг РЦ. Удаление отменено.')
        
            return        
            
        }
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'bank_accounts', 
            id:     id,
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['voc_organization_legal_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data);
        
    }

})