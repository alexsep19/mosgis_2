define ([], function () {

    return function (data, view) {
        
        $('title').text ('Категория граждан: ' + data.item.categoryname)
        
        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'citizen_compensation_category_common',   caption: 'Общие'},
//                            {id: 'citizen_compensation_category_reasons', caption: 'Условия предоставления'},
//                            {id: 'citizen_compensation_category_legal_acts', caption: 'НПА'},
//                            {id: 'citizen_compensation_category_calculation_kinds', caption: 'Порядок расчета компенсации расходов'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_citizen_compensation_category

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'citizen_compensation_category.active_tab')
            },

        });

    }

})