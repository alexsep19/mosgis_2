define ([], function () {
    
    var form_name = 'overhaul_short_program_house_common_form'

    return function (data, view) {
    
        var it = data.item
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 100},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'overhaul_short_program_house_works', caption: 'Виды работ'},
                            {id: 'overhaul_short_program_house_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_overhaul_short_program_house_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)

    }
    
})