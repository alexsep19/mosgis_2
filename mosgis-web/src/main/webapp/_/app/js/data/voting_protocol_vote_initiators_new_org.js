define ([], function () {

    $_DO.update_voting_protocol_vote_initiators_new_org = function (e) {

        var form = w2ui ['voting_protocol_vote_initiators_new_org_form']

        var v = form.values ()

        if (!v.uuid_org) die ('uuid_org', 'Пожалуйста, выберите инициатора-организацию из списка')
        
        var tia = {type: 'vote_initiators'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['voting_protocol_vote_initiators_grid']
        
        var data = clone ($('body').data ('data'))

        v['uuid_protocol'] = data.item.uuid

        query (tia, {data: v}, function (data) {
        
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