define ([], function () {

    $_DO.choose_tab_general_needs_municipal_resource = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('general_needs_municipal_resource.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'general_needs_municipal_resources', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'general_needs_municipal_resources'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})