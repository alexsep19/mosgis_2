define ([], function () {

    $_DO.choose_tab_tarifs = function (e) {
        
        var layout = w2ui ['tarifs_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('tarifs.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('tarifs.active_tab') || 'premise_usage_tarifs'

        done (data)

    }

})