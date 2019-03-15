define ([], function () {

    return function (data, view) {

        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'vc_oh_wk_type_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_vc_oh_wk_type

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'vc_oh_wk_type.active_tab')
            },

        });

    }

})