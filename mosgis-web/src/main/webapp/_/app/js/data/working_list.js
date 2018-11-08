define ([], function () {

    $_DO.choose_tab_working_list = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('working_list.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'working_lists'}, {}, function (data) {
                    
            $('body').data ('data', data)

            done (data)
                
        })

    }

})