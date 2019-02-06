define ([], function () {

    $_DO.choose_tab_account = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('account.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'accounts'}, {}, function (data) {        

            add_vocabularies (data, {
                vc_acc_types: 1,
                vc_gis_status: 1,
                vc_actions: 1,
            })
            
            var it = data.item
                        
            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})