define ([], function () {

    $_DO.cancel_annul_popup = function (e) {        
    
        w2popup.close ()        
        
        alert ('Операция отменена')
        
    }

    $_DO.update_annul_popup = function (e) {

        if (!confirm ('Внести запись об аннулировании объекта?')) return 

        var form = w2ui ['annul_popup_form']

        var v = form.values ()

        if (!v.terminationdate) die ('terminationdate', 'Необходимо указать дату аннулирования')
        if (!v.code_vc_nsi_330) die ('code_vc_nsi_330', 'Необходимо указать причину аннулирования')

        var tia = $_SESSION.delete ('tia') || {
            type: getPluralType (), 
            id: $_REQUEST.id, 
            action: 'update'
        }

        query (tia, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {terminationdate: dt_dmy (new Date ().toISOString ().substr (0, 10))}

        done (data)

    }

})