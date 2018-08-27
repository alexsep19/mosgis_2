define ([], function () {

    $_DO.update_mgmt_contract_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.additionalservicetypename) die ('additionalservicetypename', 'Укажите, пожалуйста, наименование услуги')
        if (!v.okei)                      die ('okei', 'Укажите, пожалуйста, единицы измерения')
        
        var tia = {type: 'mgmt_contracts'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['mgmt_contracts_grid']

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