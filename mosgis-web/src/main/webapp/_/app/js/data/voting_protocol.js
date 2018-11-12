define ([], function () {

    $_DO.choose_tab_voting_protocol = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voting_protocol.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'voting_protocols', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'voting_protocols'}, {}, function (d) {
            
                data.item = d.item

                if (d.cach) data.cach = d.cach

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})