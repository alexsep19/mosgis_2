
define ([], function () {
    
    return function (data, view) {           

        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'rosters_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'houses', caption: 'Жилой фонд'},
                            {id: 'mgmt_contracts', caption: 'Договоры управления', off: !($_USER.role.admin || $_USER.is_building_society () || $_USER.has_nsi_20 (1, 4))},
                            {id: 'public_property_contracts', caption: 'Договоры пользования общим имуществом', off: !($_USER.role.admin || $_USER.is_building_society () || $_USER.has_nsi_20 (1, 7, 8))},
                            {id: 'voc_organizations', caption: 'Организации'},
                            {id: 'voc_users', caption: 'Учётные записи', off: !$_USER.role.admin},
                            {id: 'licenses', caption: 'Лицензии'},
                            {id: 'supervision', caption: 'Надзор', off: !($_USER.role.admin || $_USER.has_nsi_20 (4))},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_rosters

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });

    }

});