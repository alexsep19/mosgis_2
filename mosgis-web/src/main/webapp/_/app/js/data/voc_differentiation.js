define ([], function () {

    $_DO.import_voc_differentiation = function (e) {

        if (!confirm ('Импортировать справочник?')) return
        
        var grid = w2ui ['voc_differentiation_grid']
        
        grid.lock ()        
        $_SESSION.set ('voc_differentiation_importing', 1)
        
        query ({type: 'voc_differentiation', id: null, action: 'import'}, {}, $_DO.check_voc_differentiation)

    }

    $_DO.check_voc_differentiation = function () {

        var grid = w2ui ['voc_differentiation_grid']

        query ({type: 'voc_differentiation', id: null, part: 'log'}, {}, function (d) {
        
            var is_importing = $_SESSION.get ('voc_differentiation_importing')

            if (!d.log.uuid) return is_importing ? null : $_DO.import_voc_differentiation ()
        
            if (d.log.is_over) {            
                $_SESSION.delete ('voc_differentiation_importing')
                if (is_importing && grid) use.block ('voc_differentiation')
                return
            }
                        
            setTimeout (function () {w2ui ['voc_differentiation_grid'].lock ('Импорт данных...', 1)}, 10)

            setTimeout ($_DO.check_voc_differentiation, 2000)

        })

    }

    return function (done) {        

        query ({type: 'voc_differentiation', id: null}, {}, function (data) {

            $('body').data ('data', data)
            
            add_vocabularies (data, {
                vc_nsi_268: 1,
                vc_diff_value_types: 1,
                vc_tariff_types: 1,
            })

            data.records = dia2w2uiRecords (data.root)
            
            var idx = {}; 
            
            $.each (data.records, function () {
                this.idx_tar = {}
                this.idx_268 = {}
                idx [this.id] = this
            })
            
            $.each (data.vc_diff_tariff, function () {
                idx [this.differentiationcode].idx_tar [this.id] = 1
            })

            $.each (data.vc_diff_nsi_268, function () {
                idx [this.differentiationcode].idx_268 [this.code] = 1
            })
            
            $.each (data.records, function () {
            
                this.tariffs = Object.keys (this.idx_tar)
                    .map (function (id) {return data.vc_tariff_types [id]})
                    .sort ()
                    .join (', ')
                    
                this.nsi_268 = Object.keys (this.idx_268)
                    .map (function (id) {return data.vc_nsi_268 [id]})
                    .sort ()
                    .join (', ')
                    
            })

            done (data)

        })

    }

})