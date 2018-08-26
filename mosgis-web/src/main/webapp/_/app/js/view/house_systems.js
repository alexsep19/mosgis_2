
define ([], function () {
    
    return function (data, view) {           

        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')
        
        var tabs = [
            {id: 'house_sys', caption: 'Наличие систем', _type: 'house_sys'},
        ]
        
        $.each (data.systems, function () { tabs.push ({
            id:      this.id,
            caption: this.label,
            hidden: this.off,
            _type: 'house_sys_' + this.name,
        })})

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'house_systems_layout',
            
            panels: [
                
                {type: 'main', size: 400,

                    tabs: {                    
                        tabs: tabs,
                        onClick: $_DO.choose_tab_house_systems
                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });

    }

});