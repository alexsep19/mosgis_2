
define ([], function () {
    
    return function (data, view) {           

        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')
        
        var tabs = [
            {id: 'house_premises_residental', caption: 'Жилые'},
            {id: 'house_premises_nonresidental', caption: 'Нежилые'},
        ]
        
        $(topmost_layout.el ('main')).w2relayout ({

            name: 'house_premises_layout',
            
            panels: [
                
                {type: 'main', size: 400,

                    tabs: {                    
                        tabs: tabs,
                        onClick: $_DO.choose_tab_house_premises
                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });

    }

})