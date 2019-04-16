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
                            {id: 'citizen_compensation_category_calculation_kinds', caption: 'Порядок расчета компенсации расходов'
                                , off: data.item.is_fixed
                            },
                            {id: 'citizen_compensation_category_calculation_kind_fixed', caption: 'Порядок расчета компенсации расходов'
                                , off: !data.item.is_fixed
                            },
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