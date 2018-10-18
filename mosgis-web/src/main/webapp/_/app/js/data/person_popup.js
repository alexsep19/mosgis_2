define ([], function () {

    $_DO.update_municipal_service_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.surname) die ('surname', 'Укажите, пожалуйста, фамилию')
        if (!v.firstname) die ('firstname', 'Укажите, подалуйста, имя')
        if (!v.sortorder && !confirm ('Вы уверены, что не забыли указать порядок сортировки?')) return $('#sortorder').focus ()
        
        var tia = {type: 'persons'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['persons_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})