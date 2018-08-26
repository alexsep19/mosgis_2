define ([], function () {

    $_DO.choose_tab_insurance_product = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('insurance_product.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'insurance_products', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'insurance_products'}, {}, function (d) {
            
                data.item = d.item

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})