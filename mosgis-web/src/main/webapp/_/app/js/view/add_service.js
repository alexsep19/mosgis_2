define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label)
        
        fill (view, data.item, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'add_service_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_add_service

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'add_service.active_tab')
            },

        });

    }

})