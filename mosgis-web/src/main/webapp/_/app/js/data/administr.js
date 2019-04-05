define ([], function () {

    $_DO.choose_tab_administr = function (e) {
        
        var layout = w2ui ['administr_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('administr.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('administr.active_tab') || 'voc_users'

        done (data)

    }

})


