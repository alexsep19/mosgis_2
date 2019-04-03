define ([], function () {

    $_DO.choose_tab_msp_decision_base = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('msp_decision_base.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'msp_decision_bases', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'msp_decision_bases'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})