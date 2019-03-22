define ([], function () {

    $_DO.update_general_needs_municipal_resource_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.parentcode)                   die ('parentcode', 'Укажите, пожалуйста, раздел')
        if (!v.generalmunicipalresourcename) die ('generalmunicipalresourcename', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_2)                die ('code_vc_nsi_2', 'Укажите, пожалуйста, вид коммунального ресурса')
        if (!v.okei)                         die ('okei', 'Укажите, пожалуйста, единицы измерения')
        
        var tia = {type: 'general_needs_municipal_resources'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['general_needs_municipal_resources_grid']

        query (tia, {data: v}, function () {
        
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