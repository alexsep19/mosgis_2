
define ([], function () {

    return function (data, view) {

        var topmost_layout = w2ui ['topmost_layout']

        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'tarifs_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'premise_usage_tarifs', caption: 'Размер платы за пользование жилым помещением'
                                , off: false
                            },
                            {id: 'social_norm_tarifs', caption: 'Социальная норма потребления электрической энергии'
                                , off: !($_USER.has_nsi_20(10) || $_USER.role.admin)
                            },
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_tarifs

                    }

                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

    }

});