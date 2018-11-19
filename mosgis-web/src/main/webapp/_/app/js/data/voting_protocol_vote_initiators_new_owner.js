define ([], function () {

    $_DO.update_voting_protocol_vote_initiators_new_owner = function (e) {

        var form = w2ui ['voting_protocol_vote_initiators_new_owner_form']

        var v = form.values ()

        if (!v.uuid_ind) die ('uuid_ind', 'Пожалуйста, выберите инициатора-собственника из списка')
        
        var tia = {type: 'vote_initiators'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['voting_protocol_vote_initiators_grid']
        
        var data = clone ($('body').data ('data'))

        v['uuid_protocol'] = data.item.uuid

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу собственника?').yes (function () {openTab ('/vc_person/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})