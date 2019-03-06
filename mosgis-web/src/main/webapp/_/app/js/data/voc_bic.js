define ([], function () {

    $_DO.import_voc_bic = function (e) {

        if (!confirm ('Импортировать справочник?')) return
        
        var grid = w2ui ['voc_bic_grid']
        
        grid.lock ()        
        $_SESSION.set ('voc_bic_importing', 1)
        
        query ({type: 'voc_bic', id: null, action: 'import'}, {}, $_DO.check_voc_bic)

    }

    $_DO.check_voc_bic = function () {

        var grid = w2ui ['voc_bic_grid']

        query ({type: 'voc_bic', id: null, part: 'log'}, {}, function (d) {
        
            var is_importing = $_SESSION.get ('voc_bic_importing')

            if (!d.log.uuid) return is_importing ? null : $_DO.import_voc_bic ()
        
            if (d.log.is_over) {            
                $_SESSION.delete ('voc_bic_importing')
                if (is_importing && grid) grid.reload (grid.refresh)
                return
            }
                        
            setTimeout (function () {w2ui ['voc_bic_grid'].lock ('Импорт данных...', 1)}, 10)

            setTimeout ($_DO.check_voc_bic, 2000)

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