define ([], function () {
    
    var form_name = 'overhaul_regional_program_doc_common_form'

    return function (data, view) {
    
        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=overhaul_regional_program_doc_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 250},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'overhaul_regional_program_doc_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_overhaul_regional_program_doc_common
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
                    {name: 'fullname', type: 'text'},
                    {name: 'number_', type: 'text'},
                    {name: 'date_', type: 'date'},
                    {name: 'legislature', type: 'text'},
                    {name: 'code_nsi_79', type: 'list', options: {items: data.vc_nsi_79.items}}
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})