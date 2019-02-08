define ([], function () {

    $_DO.create_account_common_items = function (e) {

        use.block ('account_item_popup')

    }
    
    $_DO.edit_account_common_items = function (e) {
    
        $_SESSION.set ('record', w2ui ['account_common_items_grid'].get (e.recid))

        use.block ('account_item_popup')

    }

    $_DO.delete_account_common_items = function (e) {

        if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

        var grid = w2ui ['account_common_items_grid']

        query ({type: 'account_items', id: grid.getSelection () [0], action: 'delete'}, {}, function (d) {
            grid.reload (grid.refresh)
        })

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data)
                
    }

})