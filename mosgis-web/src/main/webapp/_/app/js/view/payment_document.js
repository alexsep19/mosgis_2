define ([], function () {

    return function (data, view) {
        
        var it = data.item
        
        it.type_label = data.vc_pay_doc_types [it.id_type]

        it.customer_label = it ['ind_customer.label'] || it ['org_customer.label']
darn (it)        
        it.month_label = w2utils.settings.fullmonths [it.month - 1].toLowerCase ()
        
        $('title').text ('ПД ' + it.paymentdocumentnumber)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'payment_document_common', caption: 'Квитанция'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_payment_document

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
            
                clickActiveTab (this.get ('main').tabs, 'payment_document.active_tab')
                
                clickOn ($('#account_link'), function () { openTab ('/account/' + it.uuid_account) })
                
                
            },

        });

    }

})