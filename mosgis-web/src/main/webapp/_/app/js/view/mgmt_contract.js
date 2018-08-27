define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label)
        
        fill (view, data.item, $('body'))

        $('#container').w2layout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'mgmt_contract_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_mgmt_contract

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'mgmt_contract.active_tab')
            },

        });

    }

})