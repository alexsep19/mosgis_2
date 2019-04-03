define ([], function () {
    
    var form_name = 'msp_decision_base_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=msp_decision_base_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 160},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'msp_decision_base_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_msp_decision_base_common
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
                {name: 'uniquenumber', type: 'text'},
                {name: 'decisionname', type: 'text'},
                {name: 'code_vc_nsi_301', type: 'list', options: {items: data.vc_nsi_301.items}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})