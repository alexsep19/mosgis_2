define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label)
        
        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'base_decision_msp_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_base_decision_msp

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'base_decision_msp.active_tab')
            },

        });

    }

})