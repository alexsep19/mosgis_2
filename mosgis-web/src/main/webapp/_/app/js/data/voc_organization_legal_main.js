define ([], function () {

    $_DO.import_voc_organization_legal_main = function (e) {
    
        var data = $('body').data ('data')
        
        if (!data.import_types.length) die ('foo', 'Для данной организации отсутствуют варианты импорта данных из ГИС ЖКХ')
        
        use.block ('voc_organization_import_popup')
        
    }

    $_DO.refresh_voc_organization_legal_main = function (e) {
        if (!confirm('Послать в ГИС ЖКХ запрос на обновление данных об этом юридическом лице?'))
            return
        query({type: 'voc_organizations', action: 'refresh'}, {}, reload_page)
    }

    return function (done) {

        done (clone ($('body').data ('data')))
        
    }

})