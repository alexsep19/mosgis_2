define ([], function () {

    $_DO.refresh_voc_organization_legal_main = function (e) {
        if (!confirm('Послать в ГИС ЖКХ запрос на обновление данных об этом юридическом лице?'))
            return
        query({type: 'voc_organizations', action: 'refresh'}, {}, reload_page)
    }

    return function (done) {

        get_nsi ([20], done)
        
    }

})