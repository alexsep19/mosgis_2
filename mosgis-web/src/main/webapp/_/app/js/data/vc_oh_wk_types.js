define ([], function () {

    $_DO.import_vc_oh_wk_types = function (e) {

        var grid = w2ui ['vc_oh_wk_types_grid']

        $_SESSION.set ('importing', 1)
        grid.lock ('Запрос в ГИС ЖКХ...', true)

        query ({type: 'voc_overhaul_work_types', action: 'import', id: undefined}, {}, function (d) {
            var clock = setInterval (function () {
                query ({type: 'out_soap_export_nsi_item', part: 'rq', id: d.id}, {}, function (out_soap) {
                    console.log (out_soap)
                    if (out_soap.id_status < 2) return
                    clearInterval (clock)
                    grid.request ('get')
                })

            }, 5000)
        })

    }

    $_DO.create_vc_oh_wk_types = function (e) {
        $_SESSION.set ('record', {})
        use.block ('vc_oh_wk_types_popup')
    }

    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'voc_overhaul_work_types', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})