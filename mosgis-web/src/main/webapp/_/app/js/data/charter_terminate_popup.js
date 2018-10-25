define ([], function () {

    $_DO.update_charter_terminate_popup = function (e) {

        var f = w2ui ['charter_terminate_popup_form']

        var v = f.values ()
        var it = $('body').data ('data').item
        
        if (!v.terminate) die ('terminate', 'Укажите, пожалуйста, дату прекращения действия устава')
        if (v.terminate < it.date_) die ('terminate', 'Дата прекращения не может предшествовать дате государственной регистрации')
        
        if (!v.reason) die ('reason', 'Укажите, пожалуйста, причину прекращения действия устава')

        query ({type: 'charters', action: 'terminate'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})