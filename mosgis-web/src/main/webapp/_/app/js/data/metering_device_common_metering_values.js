define ([], function () {

    var grid_name = 'metering_device_common_metering_values_grid'

    $_DO.approve_metering_device_common_metering_values = function (e) {
    
        var data = clone ($('body').data ('data'))
        
        if (!data.item.meteringdeviceversionguid) die ('foo', 'Прибор учёта не опубликован в ГИС ЖКХ. Операция отменена.')
               
        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Отправить в ГИС ЖКХ показания за "' + dt_dmy (r.datevalue) + '?')) return
        
        grid.lock ()

        query ({type: 'metering_device_values', id: id, action: 'approve'}, {}, function () {
            grid.reload (grid.refresh)
        })

    }

    $_DO.create_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', {
            id_type: 1,
            datevalue: new Date ().toJSON ().slice (0, 10),
            code_vc_nsi_2: e.target.split ('_') [1]
        })

        use.block ('metering_value_popup')

    }

    $_DO.edit_metering_device_common_metering_values = function (e) {

        $_SESSION.set ('record', w2ui ['metering_device_common_metering_values_grid'].get (e.recid))

        use.block ('metering_value_popup')

    }
    
    $_DO.delete_metering_device_common_metering_values = function (e) {

        var grid = w2ui [grid_name]
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Удалить показания за "' + dt_dmy (r.datevalue) + '?')) return
        
        grid.lock ()

        query ({type: 'metering_device_values', id: grid.getSelection () [0], action: 'delete'}, {}, function (d) {
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