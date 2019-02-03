define ([], function () {

    $_DO.update_infrastructure_resources_popup = function (e) {

        var form = w2ui ['infrastructure_transportation_resources_popup_form']

        var data = clone ($('body').data ('data'))
        var v = form.values ()

        var reg_number = /^\d+(\.\d{1,3})?$/

        if (!v.code_vc_nsi_2) die ('code_vc_nsi_2', 'Укажите, пожалуйста, ресурс')
        
        if (!v.volumelosses) die ('volumelosses', 'Укажите, пожалуйста, объем потерь')
        if (!reg_number.test (v.volumelosses)) die ('volumelosses', 'Указано неверное значение объема потерь')

        if (!v.totalload) die ('totalload', 'Укажите, пожалуйста, общую величину присоединенной нагрузки')
        if (!reg_number.test (v.totalload)) die ('totalload', 'Указана неверная общая величина присоединенной нагрузки')

        if (!v.industrialload) die ('industrialload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на промышленость')
        if (!reg_number.test (v.industrialload)) die ('industrialload', 'Указана неверная величина присоединенной нагрузки, приходящейся на промышленость')

        if (!v.socialload) die ('socialload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на социальную сферу')
        if (!reg_number.test (v.socialload)) die ('socialload', 'Указана неверная величина присоединенной нагрузки, приходящейся на социальную сферу')

        if (!v.populationload) die ('populationload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на население')
        if (!reg_number.test (v.populationload)) die ('populationload', 'Указана неверная величина присоединенной нагрузки, приходящейся на население')

        v.okei = data.vc_nsi_2.items.find (x => x.id == v.code_vc_nsi_2).okei

        v.uuid_oki = $_REQUEST.id
        
        var tia = {type: 'infrastructure_transportation_resources'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['infrastructure_transportation_resources_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})