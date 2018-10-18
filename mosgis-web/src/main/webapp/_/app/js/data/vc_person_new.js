define ([], function () {

    $_DO.update_vc_person_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.surname) die ('surname', 'Укажите, пожалуйста, фамилию')
        if (!v.firstname) die ('firstname', 'Укажите, пожалуйста, имя')
        
        var tia = {type: 'vc_persons'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['vc_persons_grid']

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