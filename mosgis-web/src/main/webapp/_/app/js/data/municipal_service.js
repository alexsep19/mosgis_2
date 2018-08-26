define ([], function () {

    $_DO.choose_tab_municipal_service = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('municipal_service.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'municipal_services', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'municipal_services'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})