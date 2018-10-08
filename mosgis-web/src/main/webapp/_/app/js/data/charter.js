define ([], function () {

    $_DO.choose_tab_charter = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('charter.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'charters', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'charters'}, {}, function (d) {

                var it = data.item = d.item
                
                $('body').data ('data', data)                

                done (data) 
            
            })

        })

    }

})