define ([], function () {

    $_DO.create_settlement_docs = function (e) {

        function done() {
            use.block('settlement_docs')
        }

        var uuid_org = $_USER.has_nsi_20(2)? 'uuid_org'
            : $_USER.is_building_society() || $_USER.has_nsi_20(1) ? 'uuid_org_customer'
            : '00'


        $('body').data('supply_resource_contracts_popup.post_data', {search: [
                {field: 'id_ctr_status', operator: 'in', value: [{id: "11"},{id: "34"},{id: "40"},{id: "42"}]},
                {field: 'id_customer_type', operator: 'in', value: [{id: 5}]},
                {field: uuid_org, operator: 'in', value: [{id: $_USER.uuid_org}]}
            ], searchLogic: "AND"})

        $('body').data('supply_resource_contracts_popup.callback', function (r) {

            if (!r)
                return done()

            var v = {uuid_sr_ctr: r.uuid, uuid_org_author: $_USER.uuid_org}

            query({type: 'settlement_docs', action: 'create', id: undefined}, {data: v}, function (data) {

                w2popup.close()

                if (data.id)
                    w2confirm('Документ о состоянии расчетов ресурсоснабжения зарегистрирован. Открыть его страницу в новой вкладке?')
                            .yes(function () {
                                openTab('/settlement_doc/' + data.id)
                            })

                done()
            })
        })

        use.block('supply_resource_contracts_popup')
    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'settlement_docs', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })

    }

})