define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item ['fias.label'])
        
        fill (view, data.item, $('body'))

        $('#container').w2layout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'mgmt_contract_object_common',   caption: 'Общие'},
                            {id: 'mgmt_contract_object_docs',     caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_mgmt_contract_object

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'mgmt_contract_object.active_tab')
            },

        });

    }

})