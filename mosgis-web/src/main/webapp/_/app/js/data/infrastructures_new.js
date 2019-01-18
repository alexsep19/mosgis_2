define ([], function () {

    var form_name = 'infrastructure_form'
    var grid_name = 'code_vc_nsi_3_grid'

    $_DO.open_orgs_infrastructure_popup = function (e) {

        var f = w2ui [form_name]
        var g = w2ui [grid_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }

        saved.record.codes_nsi_3 = g.getSelection ()

        function done () {

            $('body').data ('data', saved.data)

            $_SESSION.set ('record', saved.record)

            use.block ('infrastructures_new')

        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            saved.record.manageroki = r.uuid
            saved.record.manageroki_label = r.label

            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_org_work_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.workname) die ('workname', 'Укажите, пожалуйста, наименование услуги')
        if (!v.code_vc_nsi_56) die ('code_vc_nsi_56', 'Укажите, пожалуйста, вид работ')
        if (!v.stringdimensionunit) die ('stringdimensionunit', 'Укажите, пожалуйста, единицу измерения')
        
        var vc_okei = $('body').data ('data').vc_okei
        for (var id in vc_okei) {
            if (vc_okei [id] != v.stringdimensionunit) continue
            v.okei = id
            delete v.stringdimensionunit
            break
        }
        
        if (!v.okei && !confirm ('Вы уверены, что Общероссийский классификатор единиц измерения неприменим для определения объёма данной услуги?')) return $('#stringdimensionunit').val ('').focus ()
        
        v.code_vc_nsi_67 = w2ui ['code_vc_nsi_67_grid'].getSelection ()
        
        if (!v.code_vc_nsi_67.length) die ('foo', 'Укажите, пожалуйста, по крайней мере одну обязательную работу')
                
        var tia = {type: 'org_works'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['infrastructure_grid']

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