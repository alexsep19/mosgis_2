define ([], function () {

    $_DO.choose_tab_citizen_compensation_category = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('citizen_compensation_category.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'citizen_compensation_categories', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'citizen_compensation_categories'}, {}, function (d) {
            
                data.item = d.item

                data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})