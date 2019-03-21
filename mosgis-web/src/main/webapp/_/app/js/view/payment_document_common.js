define ([], function () {
    
    var form_name = 'payment_document_common_form'

    return function (data, view) {

        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=payment_document_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 340},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'payment_document_common_log', caption: 'История изменений'},
                            {id: 'payment_document_common_additional_information', caption: 'Дополнительная информация'},
                        ],
                        onClick: $_DO.choose_tab_payment_document_common
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
            
                {name: 'paymentdocumentnumber', type: 'text'},
            
                {name: 'dateoflastreceivedpayment', type: 'date', options: {min: 0, precision: 2}},
                {name: 'paymentstaken_dt', type: 'date', options: {min: 0, precision: 2}},
                
                {name: 'debtpreviousperiods', type: 'float', options: {min: 0, precision: 2}},
                {name: 'advancebllingperiod', type: 'float', options: {min: 0, precision: 2}},               
                {name: 'totalpayablebychargeinfo', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalbypenaltiesandcourtcosts', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalpayablebypd', type: 'float', options: {min: 0, precision: 2}},
                {name: 'subsidiescompensation_', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalpayablebypdwith_da', type: 'float', options: {min: 0, precision: 2}},
                {name: 'paidcash', type: 'float', options: {min: 0, precision: 2}},
                {name: 'limitindex', type: 'float', options: {min: 0, precision: 2}},

                {name: 'sign', type: 'list', options: {items: [
                    {id: -1, text: "Задолженность"},
                    {id:  1, text: "Переплата"},
                ]}},
                
            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
                            
            })}                
            
        })

        $_F5 (data)        

    }
    
})