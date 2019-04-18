define ([], function () {

    var form_name = 'citizen_compensation_to_categories_popup_form'
    var grid_name = 'vc_svc_types_grid'

    $_DO.update_citizen_compensation_to_categories_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.uuid_cit_comp_cat) die ('uuid_cit_comp_cat', 'Укажите, пожалуйста, категорию')
        if (!v.periodfrom) die ('periodfrom', 'Укажите, пожалуйста, период')

        v.vc_service_types = w2ui [grid_name].getSelection ()
        
        if (!v.vc_service_types.length) die ('foo', 'Укажите, пожалуйста, по крайней мере один расход, подлежащий компенсации')

        var tia = {type: 'citizen_compensation_to_categories'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['citizen_compensation_to_categories_grid']

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