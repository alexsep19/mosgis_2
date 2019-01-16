define ([], function () {

    return function (data, view) {   
        
        fill (view, data, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 800,
                
                    tabs: {

                        tabs: [
                            {id: 'planned_examination_common', caption: 'Общие'},
                            {id: 'planned_examination_common_log', caption: 'История изменений'}
                        ],

                        onClick: $_DO.choose_tab_planned_examination

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        

    }

})