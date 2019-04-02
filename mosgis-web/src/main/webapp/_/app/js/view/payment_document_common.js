define ([], function () {
    
    var form_name = 'payment_document_common_form'

    return function (data, view) {

        var it = data.item              
    
        $_F5 = function (data) {

            it.__read_only = data.__read_only
                        
            var r = clone (it)

            r.sign = {id: r.debtpreviousperiods > 0 ? -1 : 1}

            w2ui [form_name].record = r
            
            function dis (name) {
                
                switch (name) {
                    case 'totalpayablebypd':
                    case 'totalpayablebypdwith_da':
                    case 'totalbypenaltiesandcourtcosts':
                        return true
                    default:
                        return data.__read_only
                }
                
            }

            $('div[data-block-name=payment_document_common] input').each (function () {            
                $(this).prop ({disabled: dis (this.name)})
            })

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
                            {id: 'payment_document_common_charge_info', caption: 'Текущие начисления', off: it.id_type != 0},
                            {id: 'payment_document_common_piecemeal_payment', caption: 'Рассрочка платежей', off: it.id_type != 0},
                            {id: 'payment_document_common_penalties', caption: 'Неустойки и судебные расходы', off: it.id_type != 0},
                            {id: 'payment_document_common_additional_information', caption: 'Дополнительная информация'},
                            {id: 'payment_document_common_log', caption: 'История изменений'},
                        ].filter (not_off),
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
//                {name: 'totalpayablebychargeinfo', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalbypenaltiesandcourtcosts', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalpayablebypd', type: 'float', options: {min: 0, precision: 2}},
                {name: 'subsidiescompensation_', type: 'float', options: {min: 0, precision: 2}},
                {name: 'totalpayablebypdwith_da', type: 'float', options: {min: 0, precision: 2}},
                {name: 'paidcash', type: 'float', options: {min: 0, precision: 2}},
//                {name: 'limitindex', type: 'float', options: {min: 0, precision: 2}},

                {name: 'sign', type: 'list', options: {items: [
                    {id: -1, text: "Задолженность"},
                    {id:  1, text: "Переплата"},
                ]}},
                
            ],
            
            onRefresh: function (e) {e.done (function () {
                            
            })}                
            
        })

        $_F5 (data)        

    }
    
})