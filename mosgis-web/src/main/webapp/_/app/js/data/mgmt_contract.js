define ([], function () {

    $_DO.choose_tab_mgmt_contract = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'mgmt_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'mgmt_contracts'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})