define ([], function () {

    return function (data, view) {
        
        $('title').text ('ДУ №' + data.item.docnum + ' от ' + dt_dmy (data.item.signingdate))
        
        fill (view, data.item, $('body'))

        $('#container').w2layout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'mgmt_contract_common',   caption: 'Общие'},
                            {id: 'mgmt_contract_docs',     caption: 'Документы'},
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