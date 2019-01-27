define([], function () {

    $_DO.edit_supply_resource_contract_object_quality_levels = function (e) {

        var grid = w2ui [e.target]

        var r = grid.get(e.recid)

        $_SESSION.set('record', r)

        use.block('supply_resource_contract_object_quality_levels_popup')
    }

    $_DO.delete_supply_resource_contract_object_quality_levels = function (e) {

        if (!e.force)
            return

        var grid = w2ui [e.target]

        grid.lock()

        query({type: 'supply_resource_contract_quality_levels', id: grid.getSelection() [0], action: 'delete'}, {}, function () {

            use.block('supply_resource_contract_object_quality_levels')

        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        var data = clone($('body').data('data'))

        query(
            {type: 'supply_resource_contract_object_quality_levels', part: 'vocs', id: undefined}, {}, function (d) {

            var subj_239 = data.subjects.map(function (i) {
                return i.code_vc_nsi_239
            })

            d.vc_nsi_276 = d.vc_nsi_276.filter(function (i) {
                return subj_239.indexOf(i['vc_nsi_239.code']) != -1
            })

            add_vocabularies(d, {
                vc_nsi_276: 1,
                vc_okei: 1,
            })

            for (k in d) {
                data[k] = d[k]
            }

            $('body').data('data', data)

            done(data)
        })

    }

})