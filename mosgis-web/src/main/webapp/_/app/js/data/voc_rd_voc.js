define ([], function () {

    function get_id () {
        return w2ui ['sidebar'].selected.substr ('rd_voc_'.length)
    }

    function get_grid () {
        var name = 'rd_cols_grid_' + get_id ()
        return w2ui [name]
    } 

    function show_progress (g) {
        var grid = get_grid () || g
        grid.lock ('Импорт...', true)        
    }

    $_DO.open_popup_voc_rd_voc = function (e) {
        
        var data = $('body').data ('data')
        
        if (!data.item.code_vc_nsi_197) return alert ('Для синхронизации справочников необходимо сопоставить данный атрибут ИС с показателем ГИС ЖКХ')
        
        if (!data.nsi.items) return alert ('Сопоставленный справочник ГИС ЖКХ "' + data.nsi.label + '" не загружен')
        
        use.block ('voc_rd_nsi_popup')
        
    }
    
    $_DO.import_voc_rd_voc = function (e) {
        
        show_progress ()
                
        query ({type: 'voc_rd_voc', id: get_id (), action: 'import'}, {}, function () {
            
            get_grid ().reload ()

        })

    }

    return function (done) {
        
        var id = get_id ()
        
        query ({type: 'voc_rd_voc', id: get_id ()}, {}, function (data) {
            
            data.id = id
            
            if (data.nsi && data.nsi.ref) data.nsi.href = '/voc_nsi/' + data.nsi.ref
                                                                                    
            $('body').data ('data', data)
            
            if (data.nsi) $.each (data.nsi.items, function () {data.nsi [this.id] = this.label})

            done (data)
            
        })

    }

})