define ([], function () {

    $_DO.choose_tab_license = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('license.active_tab', name)
            
        use.block (name)
    }

    return function (done) {

        query ({type: 'licenses'}, {}, function (data) {

            add_vocabularies (data, {
                vc_license_status: 1,
                vc_document_status: 1,
                vc_nsi_75: 1,
                vc_actions: 1,
            })
            
            $('body').data ('data', data)
            
            done (data)

        })

    }

})