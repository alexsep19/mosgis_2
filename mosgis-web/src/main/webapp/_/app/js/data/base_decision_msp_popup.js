define ([], function () {

    $_DO.update_base_decision_msp_popup = function (e) {

        var form = w2ui ['vc_nsi_302_form']

        var v = form.values ()
        
        if (!v.decisionname)                   die ('decisionname', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_301)                die ('code_vc_nsi_301', 'Укажите, пожалуйста, тип')
        
        var tia = {type: 'base_decision_msps'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['base_decision_msps_grid']

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