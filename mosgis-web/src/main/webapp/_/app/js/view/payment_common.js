define ([], function () {

    var form_name = 'payment_common_form'

    return function (data, view) {

        var it = data.item

        $_F5 = function (data) {

            it.__read_only = data.__read_only

            var r = clone (it)

            r.sign = {id: r.debtpreviousperiods > 0 ? -1 : 1}

            w2ui [form_name].record = r

            function dis (name) {

                switch (name) {
                    case 'totalpayablebypd':
                    case 'totalpayablebypdwith_da':
                    case 'totalbypenaltiesandcourtcosts':
                        return true
                    default:
                        return data.__read_only
                }

            }

            $('div[data-block-name=payment_common] input, textarea').each (function () {
                $(this).prop ({disabled: dis (this.name)})
            })

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 230},
                {type: 'main', size: 400,
                    tabs: {
                        tabs:    [
                            {id: 'payment_common_log', caption: 'История изменений', off: 1},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_payment_common
                    }
                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        $panel.w2reform ({

            name   : form_name,

            record : it,

            fields : [
                {name: 'ordernum', type: 'text'},
                {name: 'orderdate', type: 'date'},
                {name: 'amount', type: 'float', options: {min: 0, precision: 2}},
                {name: 'paymentpurpose', type: 'textarea'},
            ],

            onRefresh: function (e) {e.done (function () {

            })}

        })

        $_F5 (data)

    }

})