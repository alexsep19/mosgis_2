define ([], function () {
    
    var form_name = 'charter_payment_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            var it = data.item

            it.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=charter_payment_common] input').prop ({disabled: data.__read_only})
            $('div[data-block-name=charter_payment_common] input[name=type_]').prop ({disabled: true})

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
                
                {type: 'top', size: 180},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'charter_payment_common_service_payments', caption: 'Перечень услуг и размер оплаты'},
                            {id: 'charter_payment_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_charter_payment_common
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
                {name: 'payment_0', type: 'float', options: {min: 0}},
                {name: 'payment_1', type: 'float', options: {min: 0}},
                {name: 'uuid_file_1', type: 'list', options: {items: data.docs.items}},
                {name: 'uuid_file_0', type: 'list', options: {items: data.docs.items}},
            ],

            focus: -1,
            
            onChange: function (e) {
            
                if (/^uuid_file_/.test (e.target)) {

                    e.done (function () {

                        if (e.value_new.id == -1) {
                        
                            $_SESSION.set ('field_name', e.target)
                        
                            use.block ('charter_payment_doc_new')
                            
                        }

                    })

                }           
                
            },
            
            onRefresh: function (e) {

                e.done (function () {
                
                    var r = this.record
                    
                    if (r.__read_only) {
                    
                        $('.proto input').css ('cursor', 'pointer').click ($_DO.download_charter_payment_common)

                    }               
                
                })            
            
            }

        })

        $_F5 (data)        

    }
    
})