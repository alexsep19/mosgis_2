define ([], function () {

    $_DO.choose_tab_social_support = function (e) {
        
        var layout = w2ui ['social_support_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('social_support.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('social_support.active_tab') || 'premise_usage_social_support'

        done (data)

    }

})