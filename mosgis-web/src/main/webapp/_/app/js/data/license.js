define ([], function () {

    $_DO.choose_tab_license_legal = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('license.active_tab', name)
            
        use.block (name)
    }

    $_DO.choose_top_tab_license = function (e) {

        var name = e.tab.id

        localStorage.setItem('license.active_top_tab', name)

        use.block(name)
    }

    return function (done) {

        query ({type: 'licenses'}, {}, function (data) {

            data.item.label_delegated = data.is_delegated ? 'Да' : 'Нет'

//            add_vocabularies (data, {
//                vc_acc_req_status: 1,
//                vc_acc_req_types: 1,
//            })

            $('body').data ('data', data)

//            get_nsi([20], done)

        })

    }

})