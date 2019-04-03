define ([], function () {

    return function (data, view) {

        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'social_norm_tarif_common', caption: 'Общие'},
                            {id: 'tarif_diffs',  caption: 'Критерии дифференциации'},
                            {id: 'tarif_coeffs', caption: 'Коэффициент тарифа'},
                            {id: 'tarif_legal_acts', caption: 'НПА'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_social_norm_tarif

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'social_norm_tarif.active_tab')
            },

        });

    }

})