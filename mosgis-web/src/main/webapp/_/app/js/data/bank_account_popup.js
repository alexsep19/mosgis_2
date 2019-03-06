define ([], function () {

    var form_name = 'bank_account_popup_form'

    $_DO.update_bank_account_popup = function (e) {
/*
        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес')
        if (!(v.sharepercent >= 0.01)) die ('sharepercent', 'Укажите, пожалуйста, корректное значение доли в процентах')
                
        var tia = {type: 'account_items', id: form.record.uuid}
        
        if (tia.id) {
            tia.action = 'update'
        }
        else {
            tia.action = 'create'
            v.uuid_account = $_REQUEST.id
        }

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['account_common_items_grid']
            grid.reload (grid.refresh)
        })
*/
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record') || {

        }
                
        done (data)

    }

})