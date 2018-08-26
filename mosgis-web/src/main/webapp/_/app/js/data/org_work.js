define ([], function () {

    $_DO.choose_tab_org_work = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('org_work.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'org_works', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'org_works'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})