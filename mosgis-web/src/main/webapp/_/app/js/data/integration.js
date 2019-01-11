define ([], function () {

    $_DO.choose_tab_integration = function (e) {
        
        var layout = w2ui ['integration_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('integration.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('integration.active_tab') || 'senders'

        done (data)

    }

})