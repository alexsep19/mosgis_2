define ([], function () {

    var form_name = 'vc_oh_wk_types_popup_form'

    $_DO.update_vc_oh_wk_types_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!servicename) die ('servicename', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_218) die ('code_vc_nsi_39', 'Укажите, пожалуйста, основание управления')

        v.uuid_org = $_USER.uuid_org
                
        var tia = {type: 'voc_overhaul_work_types'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})