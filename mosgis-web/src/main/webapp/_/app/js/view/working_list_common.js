define ([], function () {
    
    var form_name = 'working_list_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

//            r.label_org_customer = customer_label (vc_gis_customer_type [r.id_customer_type], r ['org_customer.label'])
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=working_list_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

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
//                            {id: 'working_list_common_services', caption: 'Услуги'},
                            {id: 'working_list_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_working_list_common
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
                {name: 'dt_from', type: 'list', options: {items: data.begins}},
                {name: 'dt_to', type: 'list', options: {items: data.ends}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})