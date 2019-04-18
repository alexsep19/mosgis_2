
define ([], function () {

    return function (data, view) {

        var topmost_layout = w2ui ['topmost_layout']

        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'overhauls_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_regional_programs', caption: 'Региональная программа'},
                            {id: 'overhaul_short_programs', caption: 'КПР'},
                            {id: 'overhaul_address_programs', caption: 'Адресные программы'}
                        ],

                        onClick: $_DO.choose_tab_overhauls

                    }

                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

    }

});