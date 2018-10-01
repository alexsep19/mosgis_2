
define ([], function () {
    
    return function (data, view) {           
        
        fill (view, data, $('body'))
        
        if (!$_USER) return use.block ('login') 
                
        $('main').w2layout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'out_soap',     caption: 'Запросы сервиса', text: 'Tab 1', tooltip: 'Tab 1 tooltip'},
                            {id: 'voc_nsi_list', caption: 'Справочники', text: 'Tab 2', tooltip: 'Tab 2 tooltip'},
                            {id: 'rosters',      caption: 'Реестры', text: 'Tab 3', tooltip: 'Tab 3 tooltip'},
//                            {id: 'admin',        caption: 'Администрирование'},
                        ],

                        onClick: $_DO.choose_tab_main

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
    
    }

});