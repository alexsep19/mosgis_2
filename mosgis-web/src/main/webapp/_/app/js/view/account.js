define ([], function () {

    return function (data, view) {
        
        var it = data.item
        
        $('title').text ('ЛС ' + it.accountnumber)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'account_common',   caption: 'Общие'},
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