define ([], function () {

    $_DO.choose_tab_rosters = function (e) {
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('rosters.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('rosters.active_tab') || 'houses'

        done (data)

    }

})