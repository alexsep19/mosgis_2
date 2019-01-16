define ([], function () {

    var form_name = 'supply_resource_contract_new_form'

    $_DO.update_supply_resource_contract_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.id_customer_type) die('id_customer_type', 'Вы забыли указать тип заказчика')

        var f_customer = v.id_c_type == 1? 'uuid_org_customer'
            : v.id_c_type == 2 ? 'uuid_person_customer'
            : ''

        if (f_customer && !v[f_customer])
            die(f_customer, 'Вы забыли указать заказчика')

        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер')

        if (!v.signingdate) die ('signingdate', 'Вы забыли указать дату заключения')
        if (!v.code_vc_nsi_58) die('code_vc_nsi_58', 'Вы забыли указать основание')
        if (!v.effectivedate) die ('effectivedate', 'Вы забыли указать дату вступления в силу')

        query ({type: 'supply_resource_contracts', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Договор зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/supply_resource_contract/' + data.id)})

            var grid = w2ui ['supply_resource_contracts_grid']

            grid.reload (grid.refresh)

        })

    }

    $_DO.open_orgs_supply_resource_contract_org_new = function(e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {
            $('body').data('data', saved.data)
            $_SESSION.set('record', saved.record)
            use.block('supply_resource_contract_new')
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            saved.record.uuid_org_customer = r.uuid
            saved.record.label_org_customer = r.label

            done()

        })

        use.block('voc_organizations_popup')
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record')

        data.record.voc_c_types = [
            {id: 1, text: 'Юрлицо',  off: [6].indexOf(data.record.id_customer_type) != -1 },
            {id: 2, text: 'Физлицо', off: [3, 5, 6].indexOf(data.record.id_customer_type) != -1 },
            {id: 0, text: 'Отсутствует', off: [5].indexOf(data.record.id_customer_type) != -1}
        ].filter(not_off)

        if (typeof data.record.id_c_type == 'undefined') {
            data.record.id_c_type = data.record.voc_c_types [0].id
        }


        if (data.record.id_customer_type.hasOwnProperty('id')) {
            data.record.id_customer_type = data.record.id_customer_type.id
        }

        if (data.record.id_c_type.hasOwnProperty('id')) {
            data.record.id_c_type = data.record.id_c_type.id
        }


        query ({type: 'vc_persons', id: undefined}, {limit:100000, offset:0}, function (d) {

            data.persons = d.root.map (function (i) {return {
                id: i.id,
                text: i.label
            }})

            done (data)
        })



    }

})