define ([], function () {

    $_DO.choose_tab_admin = function (e) {
        
        var layout = w2ui ['admin_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('admin.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('admin.active_tab') || 'vc_rd_1'

        done (data)

    }

})