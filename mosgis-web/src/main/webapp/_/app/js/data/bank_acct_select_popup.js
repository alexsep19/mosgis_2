define ([], function () {

    var form_name = 'bank_acct_select_popup_form'

    $_DO.update_bank_acct_select_popup = function (e) {
    
        if (!confirm ('Установить выбранный счёт в качестве платёжного реквизита?')) return
    
        var form = w2ui [form_name]

        var v = form.values ()
        
        w2ui ['topmost_layout'].lock ('main')
        
        query ({type: $_REQUEST.type + 's', action: 'update'}, {data: v}, reload_page)

    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))
        data.record = {uuid_bnk_acct: data.item.uuid_bnk_acct}
        done (data)
        
    }

})