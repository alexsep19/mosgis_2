define ([], function () {

    $_DO.import_vc_oh_wk_types = function (e) {

        var grid = w2ui ['vc_oh_wk_types_grid']

        grid.lock ('Запрос в ГИС ЖКХ...', true)

        query ({type: 'voc_overhaul_work_types', part: 'import', id: undefined}, function (d) {
            grid.refresh ()
        })

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