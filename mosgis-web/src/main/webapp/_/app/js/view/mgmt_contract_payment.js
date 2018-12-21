define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text (dt_dmy (it.begindate) + '-' + dt_dmy (it.enddate) + ' ' + it ['fias.label'])
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'mgmt_contract_payment_common',   caption: 'Общие'},
//                            {id: 'mgmt_contract_payment_working_lists',     caption: 'Перечень услуг'},
//                            {id: 'mgmt_contract_payment_docs',     caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_mgmt_contract_payment

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'mgmt_contract_payment.active_tab')
            },

        });

        clickOn ($('#object_link'), function () { openTab ('/mgmt_contract_object/' + it.uuid_contract_object) })

        clickOn ($('#ctr_link'), function () {        
            openTab ('/mgmt_contract/' + it.uuid_contract)        
        })

    }

})