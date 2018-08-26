define ([], function () {
    
    var form_name = 'insurance_product_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=insurance_product_common] input, textarea').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()            

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 190},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'insurance_product_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_insurance_product_common
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
                {name: 'description',  type: 'textarea'},
                {name: 'files', type: 'file', options: {max: 1}},
                {name: 'insuranceorg', type: 'list', options: {items: data.vc_orgs_ins.items.concat ({id: 0, text: '(другая организация...)'})}},
            ],

            focus: -1,
            
            onChange: function (e) {

                if (e.target == 'insuranceorg' && e.value_new.id == 0) $_DO.open_orgs_insurance_product_common ()
                        
            }
            
        })

        $_F5 (data)                       

        clickOn ($('#file_label'), $_DO.download_insurance_product_common)

    }
    
})