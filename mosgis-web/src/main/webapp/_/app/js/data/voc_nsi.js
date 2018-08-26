define ([], function () {
    
    function get_id () {        
        return w2ui ['sidebar'] ? w2ui ['sidebar'].selected : $_REQUEST.id
    }
    
    function get_grid () {
        var name = 'nsi_grid_' + get_id ()
        return w2ui [name]
    } 
    
    function show_progress (g) {
        var grid = get_grid () || g
        grid.lock ('Импорт...', true)        
    }
    
    function is_loading (data) {
        
        var s = data.item ['out_soap.id_status']
        
        return s > 0 && s < 3;
        
    }
    
    function wait () {
        
        var int = setInterval (function () {

            query ({type: 'voc_nsi_list', id: get_id ()}, {}, function (data) {

                if (is_loading (data)) return
                
                var err = data.item ["out_soap.err_code"]                                
                
                if (err) {

                    var t = data.item ["out_soap.err_text"]

                    if (t) err += ' ' + t

                    w2alert ('При импорте спавочника из ГИС ЖКХ произошла ошибка"' + err + '"', '')

                }
                    
                clearInterval (int)
                        
                use.block ('voc_nsi')

            })
                
        }, 6000)
        
    }
    
    $_DO.check_status_voc_nsi = function (e) {
        
        if (!is_loading ($('body').data ('data'))) return;
        
        e.done (function () {
            
            show_progress (this)
            
            wait ()
            
        })
        
    }

    $_DO.import_voc_nsi = function (e) {
        
        show_progress ()
                
        query ({type: 'voc_nsi_list', id: get_id (), action: 'import'}, {}, function () {
            
            wait ()

        })

    }    

    return function (done) {
        
        query ({type: 'voc_nsi_list', id: get_id ()}, {}, function (data) {
            
            data.id = get_id ()
            
            data.records = dia2w2uiRecords (data.records || [])
            
            data.item.cols = JSON.parse (data.item.cols || '[]')
            
            var vocs = {};
            
            if (data.vc_okei) vocs.vc_okei = 1
            
            $.each (data.item.cols, function () {if (this.ref) vocs ['vc_nsi_' + this.ref] = 1})
            
            add_vocabularies (data, vocs)
            
            $('body').data ('data', data)
            
            done (data)
            
        })

    }

})