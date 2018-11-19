define ([], function () {
    
    var form_name = 'mgmt_contract_payment_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=mgmt_contract_payment_common] input').prop ({disabled: data.__read_only})
            $('div[data-block-name=mgmt_contract_payment_common] input[name=type_]').prop ({disabled: true})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 160 + 30 * data.item.is_proto},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'mgmt_contract_payment_common_service_payments', caption: 'Перечень услуг и размер оплаты'},
                            {id: 'mgmt_contract_payment_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_mgmt_contract_payment_common
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
                {name: 'housemanagementpaymentsize', type: 'float', options: {min: 0}},
                {name: 'type_', type: 'list', options: {items: data.vc_ctr_pay_types.items}},
                {name: 'uuid_voting_protocol', type: 'list', options: {items: data.voting_proto.items}},
            ],

            focus: -1,

        })

        $_F5 (data)        

    }
    
})