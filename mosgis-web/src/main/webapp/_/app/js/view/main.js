
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
                            {id: 'overhauls', caption: 'Капитальный ремонт', off: !$_USER.role.admin && !$_USER.has_nsi_20 (7, 14)},
                            {id: 'supervision',                caption: 'Надзор', off: !($_USER.role.admin || $_USER.has_nsi_20 (4))},
                            {id: 'tarifs',                     caption: 'Тарифы', text: 'Тарифы', tooltip: ''
                                , off: !($_USER.role.admin || $_USER.has_nsi_20 (7, 8, 10))
                            },
                            {id: 'administr',                           caption: 'Администрирование', text: 'Администрирование' },
                            {id: 'integration',                caption: 'Интеграция', off: !$_USER.role.admin && !$_USER.has_nsi_20 (1, 2)},
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