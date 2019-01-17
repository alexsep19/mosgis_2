define ([], function () {

    $_DO.update_reporting_period_unplanned_works_popup = function (e) {

        var form = w2ui ['reporting_period_unplanned_works_popup_form']

        var v = form.values ()

        if (!v.uuid_org_work) die ('uuid_org_work', 'Укажите, пожалуйста, вид услуги')
        if (!(v.price > 0)) die ('price', 'Укажите, пожалуйста, цену')
        if (!(v.amount > 0)) die ('amount', 'Укажите, пожалуйста, объём оказанных услуг')
        if (!(v.count > 0)) die ('count', 'Укажите, пожалуйста, количество оказанных услуг')

        switch (parseInt (v.code_vc_nsi_56)) {
            case 3:
                if (!v.code_vc_nsi_57) die ('code_vc_nsi_57', 'Укажите, пожалуйста, объект аварии')
                if (!v.accidentreason) die ('accidentreason', 'Укажите, пожалуйста, причину аварии')
                if (v.code_vc_nsi_57 > 1 && !v.code_vc_nsi_3)  die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид КУ')
                break
            case 5:
                if (!v.code_vc_nsi_3)  die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид КУ')
                if (!v.organizationguid)  die ('organizationguid', 'Укажите, пожалуйста, поставщика коммунального ресурса')
                break
        }

        var tia = {type: 'unplanned_works'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['reporting_period_unplanned_works_grid']
        
        grid.lock ()
return
        query (tia, {data: v}, function () {
        
            grid.unlock ()

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