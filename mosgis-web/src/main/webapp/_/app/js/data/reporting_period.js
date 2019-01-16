define ([], function () {

    $_DO.choose_tab_reporting_period = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('reporting_period.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'reporting_periods'}, {}, function (data) {        
        
/*        
            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                org_works: 1,
            })
*/        
            
            var it = data.item
            
            it._can = {}               
                                
            $('body').data ('data', data)            

            done (data)
                
        })

    }

})