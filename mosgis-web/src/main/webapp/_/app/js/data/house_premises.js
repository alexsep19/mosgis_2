define ([], function () {

    $_DO.choose_tab_house_premises = function (e) {
        
        var layout = w2ui ['house_premises_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('house_premises.active_tab', name)

        use.block (e.tab.id)

    }    

    return function (done) {
    
        var data = {}
        
        data.active_tab = localStorage.getItem ('house_premises.active_tab') || 'house_premises_residental'

        done (data)

    }

})