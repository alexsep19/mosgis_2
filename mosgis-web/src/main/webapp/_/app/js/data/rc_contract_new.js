define ([], function () {

    var form_name = 'rc_contract_new_form'

    $_DO.update_rc_contract_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        v.uuid_org_customer = $_USER.uuid_org

        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер')
        if (!v.signingdate) die ('signingdate', 'Вы забыли указать дату заключения')
        if (!v.effectivedate) die ('effectivedate', 'Вы забыли указать дату вступления в силу')

        if (!v.uuid_org) die('uuid_org', 'Вы забыли указать РЦ')

        query ({type: 'rc_contracts', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Договор зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/rc_contract/' + data.id)})

            var grid = w2ui ['rc_contracts_grid']

            grid.reload (grid.refresh)

        })

    }

    $_DO.open_orgs_rc_contract_new = function(e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {
            $('body').data('data', saved.data)
            $_SESSION.set('record', saved.record)
            use.block('rc_contract_new')
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            saved.record.uuid_org = r.uuid
            saved.record.label_org = r.label

            done()

        })

        use.block('voc_organizations_popup')
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {id_service_type: 1}

        done (data)
    }

})