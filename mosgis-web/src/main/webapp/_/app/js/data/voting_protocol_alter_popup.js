define ([], function () {

    $_DO.cancel_voting_protocol_alter_popup = function (e) {
    
        w2popup.close ()        
        
        alert ('Операция отменена')
        
    }

    $_DO.update_voting_protocol_alter_popup = function (e) {

        if (!confirm ('Открыть протокол на изменение?')) return

        var form = w2ui ['voting_protocol_alter_popup_form']

        var v = form.values ()

        if (!v.modification) die ('modification', 'Необходимо указать основание изменения')

        query ({type: 'voting_protocols', id: $_REQUEST.id, action: 'alter'}, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {}

        done (data)

    }

})