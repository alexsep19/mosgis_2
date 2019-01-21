define ([], function () {

    var form_name = 'supply_resource_contract_objects_new_form'

    $_DO.update_supply_resource_contract_objects_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.fiashouseguid)
            die('fiashouseguid', 'Вы забыли указать адрес')

        var data = $('body').data('data')

        v.uuid_sr_ctr = data.item.uuid

        query ({type: 'supply_resource_contract_objects', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Объект жилищного фонда зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/supply_resource_contract_object/' + data.id)})

            var grid = w2ui ['supply_resource_contract_objects_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

        done(data)

    }

})