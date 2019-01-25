define ([], function () {

    $_DO.update_voc_organization_import_popup = function (e) {

        var form = w2ui ['voc_organization_import_popup_form']

        var v = form.values ()
        
        if (!v.import_type) die ('import_type', 'Укажите, пожалуйста, тип импорта')
                
        var done = reload_page

        query ({type: 'voc_organizations', action: v.import_type}, {}, function () {
            alert ('Запрос в ГИС ЖКХ зарегистрирован')
            w2popup.close ()
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var r = {}
        
        var t = data.import_types
        
        if (t.length == 1) r.import_type = t [0].id
        
        data.record = r
                
        done (data)

    }

})