
define ([], function () {
    
    return function (data, view) {           
        
        fill (view, data, $('#body'))
        
        if (!$_USER) return use.block ('login') 
                
        $('main').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'out_soap',                   caption: 'Запросы сервиса', text: 'Запросы сервиса'},
                            {id: 'voc_nsi_list',               caption: 'Справочники', text: 'Справочники'},
                            {id: 'rosters',                    caption: 'Реестры', text: 'Реестры', tooltip: ''},
                            {id: 'overhaul_regional_programs', caption: 'Капитальный ремонт - Региональная программа', off: !$_USER.role.admin && !$_USER.has_nsi_20 (7, 14)},
                            {id: 'integration',                caption: 'Интеграция', off: !$_USER.role.admin && !$_USER.has_nsi_20 (1, 2)},
                            {id: 'supervision',                caption: 'Надзор', off: !($_USER.role.admin || $_USER.has_nsi_20 (4))},
                        ].filter (not_off),

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