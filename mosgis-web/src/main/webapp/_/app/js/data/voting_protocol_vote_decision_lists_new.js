define ([], function () {

    $_DO.update_voting_protocol_vote_decision_lists_new = function (e) {

        var form = w2ui ['voting_protocol_vote_decision_lists_new_form']

        var v = form.values ()

        //if (!v.uuid_org) die ('uuid_org', 'Пожалуйста, выберите инициатора-организацию из списка')
        
        var tia = {type: 'vote_decision_lists'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['voting_protocol_vote_decision_lists_grid']
        
        var data = clone ($('body').data ('data'))

        v['protocol_uuid'] = data.item.uuid

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()

            if (data.id) w2confirm ('Перейти на страницу повестки?').yes (function () {openTab ('/vote_decision_lists/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})