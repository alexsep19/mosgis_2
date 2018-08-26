define ([], function () {
    
    $_DO.choose_tab_main = function (e) {

        var name = e.tab.id
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('main.active_tab', name)
            
        use.block (name)
            
    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('main.active_tab') || 'out_soap'

        done (data)

    }

})