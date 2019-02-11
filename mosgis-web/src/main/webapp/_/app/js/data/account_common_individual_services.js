define ([], function () {

    $_DO.download_account_common_individual_services = function (e) {
    
        var box = w2ui ['account_common_individual_services_grid'].box
        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}
        w2utils.lock (box, label (0, 1))
        
        download ({type: 'account_individual_services', id: e.recid, action: 'download'}, {}, {
            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},
            onload: function () {w2utils.unlock (box)},
        })
    
    }    

    $_DO.create_account_common_individual_services = function (e) {

        use.block ('account_individual_service_popup')

    }
    
    $_DO.edit_account_common_individual_services = function (e) {
    
        $_SESSION.set ('record', w2ui ['account_common_individual_services_grid'].get (e.recid))

        use.block ('account_individual_service_popup')

    }

    $_DO.delete_account_common_individual_services = function (e) {

        if (!e.force) return

        $('.w2ui-message').remove ()

        e.preventDefault ()

        var grid = w2ui ['account_common_individual_services_grid']

        query ({type: 'account_individual_services', id: grid.getSelection () [0], action: 'delete'}, {}, function (d) {
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