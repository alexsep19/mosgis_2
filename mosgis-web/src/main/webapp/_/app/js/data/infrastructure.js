define ([], function () {

    $_DO.choose_tab_infrastructure = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('infrastructure.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'infrastructures', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'infrastructures'}, {}, function (d) {

                var it = data.item = d.item

                $('body').data ('data', data)

                done (data)

            })

        })

    }

})