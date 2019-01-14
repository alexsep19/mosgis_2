
define ([], function () {

    return function (data, view) {

        var topmost_layout = w2ui ['topmost_layout']

        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'integration_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'senders', caption: 'Внешние ИС'},
                            {id: 'voc_unom', caption: 'Соответствие адресов БТИ и ФИАС'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_integration

                    }

                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

    }

});