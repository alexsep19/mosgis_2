
define ([], function () {
    
    return function (data, view) {           

        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'admin_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'vc_rd_1', caption: 'Реестр объектов ГИС РД'},
                            {id: 'open_data', caption: 'Реестр адресов OpenData'},
                        ],

                        onClick: $_DO.choose_tab_admin

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });

    }

});