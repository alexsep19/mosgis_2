define ([], function () {

    return function (data, view) {

        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'legal_act_common',   caption: 'Общие'}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_legal_act

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'legal_act.active_tab')
            },

        });

    }

})