define ([], function () {
    
    var form_name = 'vc_oh_wk_type_common_form'

    return function (data, view) {
    
        var it = data.item              
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 180},
                {type: 'main', size: 400,
                    tabs: {
                        tabs:    [
                            {id: 'vc_oh_wk_type_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_vc_oh_wk_type_common
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