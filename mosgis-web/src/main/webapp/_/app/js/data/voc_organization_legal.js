define ([], function () {

    $_DO.choose_tab_voc_organization_legal = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voc_organization_legal.active_tab', name)
            
        use.block (name)
            
    }        
    
    $_DO.refresh_voc_organization_legal = function (e) {
        if (!confirm ('Послать в ГИС ЖКХ запрос на обновление данных об этом юридическом лице?')) return
        query ({type: 'voc_organizations', action: 'refresh'}, {}, reload_page)
    }

    return function (done) {

        query ({type: 'voc_organizations'}, {}, function (data) {

            $('body').data ('data', data)

            get_nsi ([20], done)

        })

    }

})