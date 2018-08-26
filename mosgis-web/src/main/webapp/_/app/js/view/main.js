
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
                            {id: 'out_soap',     caption: 'Запросы сервиса'},
                            {id: 'voc_nsi_list', caption: 'Справочники'},
                            {id: 'rosters',      caption: 'Реестры'},
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