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
                            {id: 'msp_decision_base_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_msp_decision_base

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'msp_decision_base.active_tab')
            },

        });

    }

})