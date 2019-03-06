define ([], function () {

    $_DO.create_person_supply_resource_contracts = function (e) {

        use.block ('supply_resource_contract_person_new')
    }

    $_DO.create_org_supply_resource_contracts = function (e) {

        use.block ('supply_resource_contract_org_new')

    }
    $_DO.import_supply_resource_contracts = function (e) {
        use.block('supply_resource_contract_import_popup')
    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'supply_resource_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })

    }

})