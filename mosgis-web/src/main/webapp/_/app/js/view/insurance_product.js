define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label)
        
        fill (view, data.item, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'insurance_product_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_insurance_product

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'insurance_product.active_tab')
            },

        });

    }

})