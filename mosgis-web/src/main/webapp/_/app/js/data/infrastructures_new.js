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

        var search_values = [{id: "2"}]
        if ($_USER.role.admin) search_values.push ({id: "8"})

        $('body').data ('voc_organizations_popup.post_data', {search:[
            {field: 'code_vc_nsi_20', operator: 'in', value: search_values}
        ], searchLogic: "AND"})
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            saved.record.manageroki = r.uuid
            saved.record.manageroki_label = r.label

            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_infrastructures_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        delete v.manageroki_label
        
        if (!v.name) die ('name', 'Укажите, пожалуйста, наименование объекта')
        if (!v.code_vc_nsi_39) die ('code_vc_nsi_39', 'Укажите, пожалуйста, основание управления')
        if (!v.hasOwnProperty('indefinitemanagement')) die ('indefinitemanagement', 'Укажите, пожалуйста, признак бессрочности управления')
        if (!v.indefinitemanagement && !v.endmanagmentdate) die ('endmanagmentdate', 'Укажите, пожалуйста, дату окончания управления')
        if (!v.code_vc_nsi_33) die ('code_vc_nsi_33', 'Укажите, пожалуйста, вид объекта')
        
        v.code_vc_nsi_3 = w2ui [grid_name].getSelection ()
        
        if (!v.code_vc_nsi_3.length) die ('foo', 'Укажите, пожалуйста, по крайней мере один вид коммунальной услуги')
                
        var tia = {type: 'infrastructures'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['infrastructures_grid']

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