define ([], function () {

    $_DO.create_rc_contracts = function (e) {

        use.block ('rc_contract_new')
    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'rc_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            data._can = {
                add: $_USER.has_nsi_20(1, 2, 8) || $_USER.is_building_society()
            }

            done (data)

        })

    }

})