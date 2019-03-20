define ([], function () {

    return function (data, view) {
        
        var it = data.item
        
        it.type_label = data.vc_acc_types [it.id_type]
        
        $('title').text ('ЛС ' + it.accountnumber)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'account_common',   caption: 'Общие'},
                            {id: 'account_payment_documents', caption: 'Платёжные документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_account

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'account.active_tab')
            },

        });

    }

})