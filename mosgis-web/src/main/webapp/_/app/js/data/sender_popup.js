define ([], function () {

    $_DO.update_sender_popup = function (e) {

        var form = w2ui ['sender_form']

        var v = form.values ()
        
        if (!v.label) die ('label', 'Укажите, пожалуйста, сокращённое наименование информационной системы')
        
        v.label_full = v.label
        
        var tia = {type: 'senders'}
        tia.action = 'create'
        
        var done = reload_page
        
        query (tia, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {}
        
        done (data)

    }

})