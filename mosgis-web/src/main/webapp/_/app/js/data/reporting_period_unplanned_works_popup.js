define ([], function () {

    $_DO.update_reporting_period_unplanned_works_popup = function (e) {

        var form = w2ui ['reporting_period_unplanned_works_popup_form']

        var v = form.values ()
        
        if (!v.additionalservicetypename) die ('additionalservicetypename', 'Укажите, пожалуйста, наименование услуги')
        if (!v.okei)                      die ('okei', 'Укажите, пожалуйста, единицы измерения')
        
        var tia = {type: 'add_services'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['add_services_grid']

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