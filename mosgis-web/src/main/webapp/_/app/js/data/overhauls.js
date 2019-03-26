define ([], function () {

    $_DO.choose_tab_overhauls = function (e) {
        
        var layout = w2ui ['overhauls_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('overhauls.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('overhauls.active_tab') || 'overhaul_regional_programs'

        done (data)

    }

})