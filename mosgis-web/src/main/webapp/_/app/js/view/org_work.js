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
                            {id: 'org_work_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_org_work

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'org_work.active_tab')
            },

        });

    }

})