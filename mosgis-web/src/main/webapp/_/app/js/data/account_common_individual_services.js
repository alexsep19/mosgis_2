define ([], function () {

    var grid_name = 'account_common_individual_services_grid'

    $_DO.download_account_common_individual_services = function (e) {
    
        var box = w2ui [grid_name].box
        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}
        w2utils.lock (box, label (0, 1))
        
        download ({type: 'account_individual_services', id: e.recid, action: 'download'}, {}, {
            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},
            onload: function () {w2utils.unlock (box)},
        })
    
    }    
    
    $_DO.approve_account_common_individual_services = function (e) {
        
        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Отправить в ГИС информацию об услуге "' + r ['svc.label'] + '" с ' + dt_dmy (r.begindate) + ' по ' + dt_dmy (r.enddate) + '?')) return
        
        grid.lock ()
        
        query ({type: 'account_individual_services', id: id, action: 'approve'}, {}, function () {
            grid.reload (grid.refresh)
        })
        
    }    

    $_DO.create_account_common_individual_services = function (e) {

        use.block ('account_individual_service_popup')

    }

    $_DO.edit_account_common_individual_services = function (e) {    
    
        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        function edit () {
            $_SESSION.set ('record', r)
            return use.block ('account_individual_service_popup')
        }
       
        switch (r.id_ctr_status) {        
        
            case 10:
            case 11:
                return edit ()
            case 40:
                if (!confirm ('Вернуть на редактирование  информацию об услуге "' + r ['svc.label'] + '" с ' + dt_dmy (r.begindate) + ' по ' + dt_dmy (r.enddate) + '?')) return
            case 14:
            case 104:
                grid.lock ()
                query ({type: 'account_individual_services', id: id, action: 'alter'}, {}, function () {
                    grid.unlock ()
                    grid.reload (grid.refresh)
                    edit ()
                })

        }

    }

    $_DO.delete_account_common_individual_services = function (e) {

        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)

        if (!confirm ('Удалить информацию об услуге "' + r ['svc.label'] + '" с ' + dt_dmy (r.begindate) + ' по ' + dt_dmy (r.enddate) + '?')) return

        return query ({type: 'account_individual_services', id: id, action: 'delete'}, {}, function (d) {
            grid.reload (grid.refresh)
        })        

    }
    
    $_DO.annul_account_common_individual_services = function (e) {

        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Удалить без возможности восстановления информацию об услуге "' + r ['svc.label'] + '" с ' + dt_dmy (r.begindate) + ' по ' + dt_dmy (r.enddate) + '?')) return
            
        return query ({type: 'account_individual_services', id: id, action: 'annul'}, {}, function (d) {
            grid.reload (grid.refresh)
        })        

    }    

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        done (data)

    }

})