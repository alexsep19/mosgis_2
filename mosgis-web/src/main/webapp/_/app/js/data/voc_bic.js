define ([], function () {

    $_DO.import_voc_bic = function (e) {

        if (!confirm ('Обновить справочник?')) return
        
        var grid = w2ui ['voc_bic_grid']
        
        grid.lock ()
        
        query ({type: 'voc_bic', id: null, action: 'import'}, {}, function () {

            grid.reload (grid.refresh)

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