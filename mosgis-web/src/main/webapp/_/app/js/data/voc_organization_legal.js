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

    $_DO.choose_top_tab_voc_organization_legal = function (e) {

        var name = e.tab.id

        localStorage.setItem('voc_organization_legal.active_top_tab', name)

        use.block(name)
    }

    return function (done) {

        query ({type: 'voc_organizations'}, {}, function (data) {

            add_vocabularies (data, {
                vc_acc_req_status: 1,
                vc_acc_req_types: 1,
            })

            $('body').data ('data', data)

            get_nsi([20], done)

        })

    }

})