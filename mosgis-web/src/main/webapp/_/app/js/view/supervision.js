define ([], function () {

    return function (data, view) {

        $(w2ui ['rosters_layout'].el ('main')).w2relayout ({
        
            name: 'supervision_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'check_plans',  caption: 'Планы проверок'},
                        ],

                        onClick: $_DO.choose_tab_supervision

                    }
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });
        

    }

})