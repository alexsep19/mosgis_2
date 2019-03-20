define ([], function () {

    $_DO.cancel_lock_voc_user_popup = function (e) {        
    
        w2popup.close ()        
        
        alert ('Операция отменена')
        
    }

    $_DO.update_lock_voc_user_popup = function (e) {

        var form = w2ui ['lock_voc_user_form']

        var v = form.values ()

        if (!v.lockreason) die ('lockreason', 'Необходимо указать причину блокировки')

        var grid = w2ui ['voc_organizations_grid']
        var id = grid.getSelection () [0]

        var tia = {
            type: 'voc_users', 
            id: id, 
            action: 'lock'
        }

        query (tia, {data: v}, reload_page)

    }
    
    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})