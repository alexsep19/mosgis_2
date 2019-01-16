define([], function () {

    $_DO.create_supply_resource_contract_subjects = function (e) {

        use.block('supply_resource_contract_subjects_new')
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        query({type: 'supply_resource_contract_subjects', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies(d, {
                vc_nsi_239: 1,
                vc_nsi_3: 1,
                vc_okei: 1
            })

            var data = clone($('body').data('data'))

            for (k in d) {
                data[k] = d[k]
            }

            done(data)
        })

    }

})