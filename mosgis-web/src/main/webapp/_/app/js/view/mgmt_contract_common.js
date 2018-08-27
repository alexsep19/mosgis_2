define ([], function () {
    
    var form_name = 'mgmt_contract_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            $.each (f.fields, function () {
            
                if (this.type != 'date') return
                
                var dt = r [this.name]
                
                if (dt.charAt (3) != '.') r [this.name] = dt_dmy (dt)
                        
            })

            f.record = r
            
            $('div[data-block-name=mgmt_contract_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

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
//                            {id: 'mgmt_contract_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_mgmt_contract_common
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
                    {name: 'docnum', type: 'text'},
                    {name: 'signingdate', type: 'date'},
                    {name: 'effectivedate', type: 'date'},
                    {name: 'plandatecomptetion', type: 'date'},
                    {name: 'code_vc_nsi_58', type: 'list', options: {items: data.vc_nsi_58.items}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})