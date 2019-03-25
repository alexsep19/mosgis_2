define ([], function () {

    return function (data, view) {

        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'premise_usage_tarif_common', caption: 'Общие'},
                            {id: 'tarif_diffs',  caption: 'Критерии дифференциации'}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_premise_usage_tarif

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'premise_usage_tarif.active_tab')
            },

        });

    }

})