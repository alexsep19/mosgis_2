define ([], function () {

    $_DO.update_metering_value_popup = function (e) {

        var f = w2ui ['metering_value_popup_form']

        var v = f.values ()
        
        if (!v.id_type) die ('id_type', 'Укажите, пожалуйста, тип показаний')
        if (!v.datevalue) die ('datevalue', 'Укажите, пожалуйста, дату снятия показаний')
        if (!(v.meteringvaluet1 > 0)) die ('meteringvaluet1', 'Укажите, пожалуйста, корректное значение')
        
        var r = f.record

        v.code_vc_nsi_2 = r.code_vc_nsi_2

        var data = clone ($('body').data ('data'))
        data.record = $_SESSION.delete ('record')
        var it = data.item
        
        if (it.tariffcount > 1 && !(v.meteringvaluet2 > 0)) die ('meteringvaluet2', 'Укажите, пожалуйста, корректное значение')
        if (it.tariffcount > 2 && !(v.meteringvaluet3 > 0)) die ('meteringvaluet3', 'Укажите, пожалуйста, корректное значение')

        var grid = w2ui ['metering_device_common_metering_values_grid']
        
        var tia = {type: 'metering_device_values'}
        
        if (tia.id = r.uuid) {
            tia.action = 'update'
        }
        else {
            tia.action = 'create'
            v.uuid_meter = $_REQUEST.id
        }

        query (tia, {data: v}, function (d) {
            w2popup.close ()
            grid.reload (grid.refresh)
        })

    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))
            
        data.record = $_SESSION.delete ('record')
        
        var it = data.item

        data.record.is_2 = it.tariffcount > 1
        data.record.is_3 = it.tariffcount > 2
        
        data.record.label_t1 = it.tariffcount > 1 ? 'T1' : 'Значение'

        done (data)
        
    }
    
})