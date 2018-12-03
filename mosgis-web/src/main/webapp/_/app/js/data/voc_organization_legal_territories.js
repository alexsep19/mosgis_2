define ([], function () {

    $_DO.choose_tab_voc_organization_legal_territories = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voc_organization_legal_territories.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'voc_organization_territories'}, {}, function (d) {

            $('body').data ('data', d)

            done (d)

        })

    }

})