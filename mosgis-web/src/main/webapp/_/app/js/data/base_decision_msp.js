define ([], function () {

    $_DO.choose_tab_base_decision_msp = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('base_decision_msp.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'base_decision_msps', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'base_decision_msps'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})