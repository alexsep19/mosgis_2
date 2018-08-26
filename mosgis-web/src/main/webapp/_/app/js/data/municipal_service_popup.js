define ([], function () {

    $_DO.update_municipal_service_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.mainmunicipalservicename) die ('mainmunicipalservicename', 'Укажите, пожалуйста, наименование услуги')
        if (!v.code_vc_nsi_3) die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид коммунальной услуги')
        if (!v.code_vc_nsi_2) die ('code_vc_nsi_2', 'Укажите, пожалуйста, вид коммунального ресурса')
        if (!v.sortorder && !confirm ('Вы уверены, что не забыли указать порядок сортировки?')) return $('#sortorder').focus ()
        
        var tia = {type: 'municipal_services'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['municipal_services_grid']

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