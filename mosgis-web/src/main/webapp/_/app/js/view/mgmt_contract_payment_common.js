define ([], function () {
    
    var form_name = 'mgmt_contract_payment_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            var it = data.item

            it.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=mgmt_contract_payment_common] input').prop ({disabled: data.__read_only})
            $('div[data-block-name=mgmt_contract_payment_common] input[name=type_]').prop ({disabled: true})

            f.refresh ()
/*            
//            if (it.uuid_file && data.__read_only) {
            
                setTimeout (function () {

                    clickOn ($('#proto input').parent (), function () {

                        alert (1)

                    })

                }, 1000)             
                
//            }
*/
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
            
            onChange: function (e) {
            
                if (e.target == "uuid_voting_protocol") {
                
                    e.done (function () {
                    
                        if (!e.value_new.id) use.block ('mgmt_contract_payment_doc_new')
                    
                    })
                
                }           
                
            },
            
            onRefresh: function (e) {

                e.done (function () {
                
                    var r = this.record
                    
                    if (r.__read_only) {
                    
                        $('#proto input').css ('cursor', 'pointer').click ($_DO.download_mgmt_contract_payment_common)

                    }               
                
                })            
            
            }

        })

        $_F5 (data)        

    }
    
})