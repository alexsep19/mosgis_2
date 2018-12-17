define ([], function () {
    
    var form_name = 'property_document_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=property_document_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 280},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'property_document_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_property_document_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var it = data.item
        
        if (it.uuid_org == $_USER.uuid_org) it.is_own = 1
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, it, $panel)        
                
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                {name: 'no', type: 'text'},
                {name: 'dt', type: 'date'},
                {name: 'issuer', type: 'text'},
                {name: 'dt_to', type: 'date'},
                {name: 'prc', type: 'float', options: {min: 0, max: 100}},
                {name: 'id_type', type: 'list', options: {items: data.vc_prop_doc_types.items}},
                {name: 'author_label', type: 'text'},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})