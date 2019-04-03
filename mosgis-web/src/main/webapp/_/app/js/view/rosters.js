
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
                            {id: 'infrastructures', caption: 'Паспорт ОКИ', off: !($_USER.role.admin || $_USER.has_nsi_20 (2, 7, 8))},
                            {id: 'mgmt_contracts', caption: 'Договоры управления', off: !($_USER.role.admin || $_USER.is_building_society () || $_USER.has_nsi_20 (1, 4, 7))},
                            {id: 'public_property_contracts', caption: 'Договоры пользования общим имуществом', off: !($_USER.role.admin || $_USER.is_building_society () || $_USER.has_nsi_20 (1, 7, 8))},
                            {id: 'supply_resource_contracts', caption: 'Договоры ресурсоснабжения'
                                , off: !($_USER.role.admin || $_USER.is_building_society() || $_USER.has_nsi_20(1, 2, 4, 7, 8))
                            },
                            {id: 'rc_contracts', caption: 'Договоры услуг РЦ'
                                , off: !($_USER.role.admin || $_USER.is_building_society() || $_USER.has_nsi_20(1, 2, 7, 8, 36))
                            },
                            {id: 'voc_organizations', caption: 'Организации'},                            
                            {id: 'metering_devices', caption: 'ПУ'},
                            {id: 'voc_users', caption: 'УЗ', off: !$_USER.role.admin},
//                            {id: 'licenses', caption: 'Лицензии'},
                            {id: 'legal_acts', caption: 'НПА'
                                , off: !($_USER.role.admin || $_USER.has_nsi_20(7, 10))
                            },
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