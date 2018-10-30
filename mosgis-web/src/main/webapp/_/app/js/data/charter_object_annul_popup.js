define ([], function () {

    $_DO.cancel_charter_object_annul_popup = function (e) {        
    
        w2popup.close ()        
        
        alert ('Операция отменена')
        
    }

    $_DO.update_charter_object_annul_popup = function (e) {

        if (!confirm ('Внести запись об аннулировании объекта?')) return 

        var form = w2ui ['charter_object_annul_popup_form']

        var v = form.values ()

        if (!v.annulmentinfo) die ('annulmentinfo', 'Необходимо указать причину аннулирования')

        query ({type: 'charter_objects', id: $_REQUEST.id, action: 'annul'}, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {}

        done (data)

    }

})