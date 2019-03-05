define ([], function () {
/*
    $_DO.choose_tab_voc_oktmo = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voc_oktmo.active_tab', name)
            
        use.block (name)
            
    }            
*/
    return function (done) {        

        query ({type: 'voc_bic', id: null, part: 'vocs'}, {}, function (data) {

            $('body').data ('data', data)
            
            add_vocabularies (data, {
                vc_nsi_237: 1
            })

            done (data)

        })

    }

})