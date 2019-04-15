define ([], function () {
    
    var form_name = 'overhaul_short_program_common_form'

    return function (data, view) {
    
        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=overhaul_short_program_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 156},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'overhaul_short_program_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_overhaul_short_program_common
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
            
            record : it,                
            
            fields : [            
                    {name: 'programname', type: 'text'},
                    {name: 'startyear', type: 'text'},
                    {name: 'startmonth', type: 'text'},
                    {name: 'endyear', type: 'text'},
                    {name: 'endmonth', type: 'text'},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})