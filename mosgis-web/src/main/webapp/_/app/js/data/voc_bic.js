define ([], function () {

    $_DO.import_voc_bic = function (e) {

        if (!confirm ('Обновить справочник?')) return
        
        var grid = w2ui ['voc_bic_grid']
        
        grid.lock ()
        
        query ({type: 'voc_bic', id: null, action: 'import'}, {}, function () {

            grid.unlock ()
            grid.reload (grid.refresh)

        })

    }

    $_DO.check_voc_bic = function () {

        var grid = w2ui ['voc_bic_grid']

        query ({type: 'voc_bic', id: null, part: 'log'}, {}, function (d) {
        
            if (d.log.is_over) {
                if (grid) grid.unlock ()
                return
            }
                        
            setTimeout (function () {w2ui ['voc_bic_grid'].lock ('Импорт данных...', 1)}, 10)

            setTimeout ($_DO.check_voc_bic, 1000)

        })

    }

    return function (done) {        

        query ({type: 'voc_bic', id: null, part: 'vocs'}, {}, function (data) {

            $('body').data ('data', data)
            
            add_vocabularies (data, {
                vc_nsi_237: 1
            })

            done (data)

        })

    }

})