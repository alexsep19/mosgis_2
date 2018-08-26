define ([], function () {
        
    return function (data, view) {
    
        $('body').data ('data', data)
                
        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')
                
        var layout = $(topmost_layout.el ('main')).w2relayout ({

            name: 'out_soap_layout',

            panels: [
                {type: 'top', size: 120},
                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'out_soap_0', caption: 'Реквизиты УО'},
                            {id: 'out_soap_export_nsi_item', caption: 'Импорт справочника'},
                            {id: 'out_soap_2', caption: 'Экспорт справочника услуг'},
                            {id: 'out_soap_3', caption: 'Импорт справочника услуг'},
                            {id: 'out_soap_4', caption: 'Дома'},
                            {id: 'out_soap_5', caption: 'Лицевые счета'},
                            {id: 'out_soap_6', caption: 'Начисления'},
                            {id: 'out_soap_7', caption: 'Приборы учёта'},
                            {id: 'out_soap_8', caption: 'Показания ПУ'},
                        ],

                        onClick: $_DO.choose_tab_out_soap

                    }                

                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            }
            
        });

        var $panel = $(layout.el ('top'))
        
        fill (view, data, $panel)
                
        $panel.w2reform ({ 
                name   : 'out_soap_filter_form',
                record : data.record,                
                fields : [               
                    {name: 'year', type: 'list', options: {items: data.years}},                    
                    {name: 'month', type: 'enum', options: {items: data.months}},
                    {name: 'is_failed', type: 'radio'},
                ],
                focus: -1,
        })
        
        $('button[name=show]').click ($_DO.show_out_soap)
                        
    }

});