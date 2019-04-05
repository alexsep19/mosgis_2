define ([], function () {
    
    var form_name = 'base_decision_msp_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=base_decision_msp_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 310},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'base_decision_msp_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_base_decision_msp_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        

        data.voc_bool = [
            {id: "0", text: "Нет"},
            {id: "1", text: "Да"}
        ]

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                {name: 'code', type: 'text'},
                {name: 'decisionname', type: 'text'},
                {name: 'code_vc_nsi_301', type: 'list', options: {items: data.vc_nsi_301.items}},
                {name: 'isappliedtosubsidiaries', type: 'list', options: {items: data.voc_bool}},
                {name: 'isappliedtorefundofcharges', type: 'list', options: {items: data.voc_bool}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})