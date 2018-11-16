define ([], function () {

    $_DO.update_voting_protocol_vote_initiators_new_owner = function (e) {

        var form = w2ui ['voting_protocol_vote_initiators_new_owner_form']

        var v = form.values ()
        
        var tia = {type: 'vote_initiators'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['voting_protocol_vote_initiators_grid']
        
        var data = clone ($('body').data ('data'))

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу инициатора?').yes (function () {openTab ('/vote_initiators/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})