define ([], function () {

    function get_id () {
        return w2ui ['sidebar'].selected.substr ('rd_cols_'.length)
    }
    
    function get_grid () {
        var name = 'rd_cols_grid_' + get_id ()
        return w2ui [name]
    } 

    function show_progress (g) {
        var grid = get_grid () || g
        grid.lock ('Импорт...', true)        
    }

    $_DO.import_voc_rd = function (e) {
        
        show_progress ()
                
        query ({type: 'voc_rd_list', id: get_id (), action: 'import'}, {}, function () {
            
            get_grid ().reload ()

        })

    }    
    
    $_DO.check_voc_rd = function (e) {
                
        var n = e.value_new

        if (!n.id) return
                
        var tn = n.tp
        
        if (!tn) {                    
            e.preventDefault ()
            alert ('Выбранная строка — не отдельное поле, а заголовок раздела. Операция отменена')
            return 
        }

        var r = get_grid ().get (e.recid)
                
        var to = r ['vc_rd_col_types.gislabel']
                
        if (to == tn) return 
                                
        if (!confirm ('Указано "' + tn + '" как источник данных для поля с типом "' + to + '". Вы уверены, что здесь нет ошибки?')) e.preventDefault ()

    }

    $_DO.update_voc_rd = function (e) {
        
        e.preventDefault ()
        
        if (!confirm ('Сохранить изменения?')) return
                        
        get_grid ().lock ('Сохранение...', true)
            
        var data = {vc_rd_cols: e.changes.map (function (i) {return {
            id: i.recid,
            code_vc_nsi_197: i.code_vc_nsi_197.id
        }})}

        query ({type: 'voc_rd_list', action: 'update'}, {data: data}, function () {

            get_grid ().reload ()

        })

    }

    return function (done) {
        
        var id = get_id ()
        
        query ({type: 'voc_rd_list', id: get_id ()}, {}, function (data) {
            
            data.id = id
            
            $.each (data.vc_nsi_197, function () {
                
                this.ord_src = this.ord
                
                this.ord = this.ord.split ('.').map (function (s) {return '0000'.substr (0, 3 - s.length) + s}).join ('')
                
                this.label = this.ord_src + '. ' + this.label
                
            })
            
            data.vc_nsi_197.sort (function (a, b) {
                return a.ord < b.ord ? -1 : a.ord > b.ord ? 1 : 0
            })            
            
            data.vc_nsi_197.unshift ({id: '', label: '(пусто)'})
            
            add_vocabularies (data, {vc_nsi_197: 1})
            
            delete data.vc_nsi_197 ['']
                                                
            $('body').data ('data', data)
                        
            done (data)
            
        })

    }

})