define ([], function () {
    
    var form_name = 'reporting_period_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=reporting_period_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 100},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs: [
                            {id: 'reporting_period_planned_works', caption: 'Информация об услугах и работах'},
                            {id: 'reporting_period_unplanned_works', caption: 'Внеплановые работы'},
                            {id: 'reporting_period_common_log', caption: 'История синхронизации'},
                        ],
                        onClick: $_DO.choose_tab_reporting_period_common
                    }
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
                
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,
            
            fields : [                     
                {name: 'year', type: 'text'},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})