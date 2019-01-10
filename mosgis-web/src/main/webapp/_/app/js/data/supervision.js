define ([], function () {

    $_DO.choose_tab_supervision = function (e) {
        
        var layout = w2ui ['supervision_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('supervision.active_tab', name)

        use.block (name)

    }    

    return function (done) { 

        var data = {}

        data.active_tab = localStorage.getItem ('supervision.active_tab') || 'check_plans'

        done (data)

    }

})