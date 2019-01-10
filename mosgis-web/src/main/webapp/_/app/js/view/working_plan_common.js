define ([], function () {
    
    var form_name = 'working_plan_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=working_plan_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 130},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs: [
                            {id: 'working_plan_common_plan', caption: 'Перечень услуг и работ'},
                            {id: 'working_plan_common_log', caption: 'История синхронизации'},
                        ],
                        onClick: $_DO.choose_tab_working_plan_common
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