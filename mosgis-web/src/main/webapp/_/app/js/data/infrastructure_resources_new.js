define ([], function () {

    $_DO.update_infrastructure_resources_new = function (e) {

        var form = w2ui ['infrastructure_resources_new_form']

        var v = form.values ()

        if (!v.code_vc_nsi_2) die ('code_vc_nsi_2', 'Укажите, пожалуйста, ресурс')
        if (!v.setpower) die ('setpower', 'Укажите, пожалуйста, установленную мощность')
        if (!v.sitingpower) die ('sitingpower', 'Укажите, пожалуйста, располагаемую мощность')
        if (!v.totalload) die ('totalload', 'Укажите, пожалуйста, общую величину присоединенной нагрузки')
        if (!v.industrialload) die ('industrialload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на промышленость')
        if (!v.socialload) die ('socialload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на социальную сферу')
        if (!v.populationload) die ('populationload', 'Укажите, пожалуйста, величину присоединенной нагрузки, приходящейся на население')

        v.okei = data.vc_nsi_2.items.find (x => x.id == v.code_vc_nsi_2).okei

        v.uuid_oki = $_REQUEST.id
        
        var tia = {type: 'infrastructure_resources'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['infrastructure_resources_grid']

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