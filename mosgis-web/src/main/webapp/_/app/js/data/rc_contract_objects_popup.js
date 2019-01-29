define ([], function () {

    var form_name = 'rc_contract_objects_popup_form'

    $_DO.update_rc_contract_objects_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        delete v.id_ctr_status

        if (!v.fiashouseguid)
            die('fiashouseguid', 'Вы забыли указать адрес')

        if (!v.dt_from)
            die('dt_from', 'Вы забыли указать дату начала')

        var data = $('body').data('data')

        var tia = {type: 'rc_contract_objects', action: 'create', id: undefined}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'

        if (tia.action == 'create') {
            v.uuid_rc_ctr = data.item.uuid
        }

        query (tia, {data: v}, function (data) {

            w2popup.close ()

            var grid = w2ui ['rc_contract_objects_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {id_ctr_status: 10}

        done(data)

    }

})