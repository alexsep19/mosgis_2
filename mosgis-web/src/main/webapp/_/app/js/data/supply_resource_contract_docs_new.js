define ([], function () {

    $_DO.update_supply_resource_contract_docs_new = function (e) {

        var f = w2ui ['supply_resource_contract_docs_new_form']

        var v = f.values ()

        if (!v ['files']) die ('files', 'Укажите, пожалуйста, файл')

        v.protocolnum = null
        v.protocoldate = null

        v.files[0].file = new File ([v.files[0].file], v.label, {type: v.files[0].file.type})

        var file = get_valid_gis_file (v, 'files')

        Base64file.upload (file, {

            type: 'supply_resource_contract_docs',

            data: {
                uuid_sr_ctr: $_REQUEST.id,
                description: v.description,
                id_type: v.id_type
            },

            onprogress: show_popup_progress (file.size),

            onloadend: function (id) {
                w2popup.close ()

                var grid = w2ui ['supply_resource_contract_docs_grid']
                grid.reload (grid.refresh)
            }

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {id_type: 1}

        done (data)

    }

})