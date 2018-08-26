define ([], function () {

    $_DO.update_entrance_popup = function (e) {
    
        var form = w2ui ['entrance_popup_form']
        
        var v = form.values ()
        
        if (!v.entrancenum) die ('entrancenum', 'Номер подъезда не может быть пустой строкой')
        
        query ({type: 'entrances', id: form.record.id, action: 'update'}, {data: v}, reload_page)
        
    }

    return function (done) {
    
        var data = $('body').data ('data')
        
        var grid = w2ui ['house_passport_entrances_grid']
        
        data.entrance = grid.get (grid.getSelection () [0])
        
        if (data.entrance.terminationdate) data.entrance.terminationdate = dt_dmy (data.entrance.terminationdate.substr (0, 10))
        
        done (data)
        
    }
    
})