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
                            {id: 'general_needs_municipal_resource_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_general_needs_municipal_resource

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'general_needs_municipal_resource.active_tab')
            },

        });

    }

})