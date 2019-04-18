define ([], function () {

    var form_name = 'acknowledgment_common_form'

    return function (data, view) {

        var it = data.item

        $_F5 = function (data) {

            it.__read_only = 1// data.__read_only

            var r = clone (it)

            r.sign = {id: r.debtpreviousperiods > 0 ? -1 : 1}

            w2ui [form_name].record = r

            $('div[data-block-name=acknowledgment_common] input, textarea').each (function () {
                $(this).prop ({disabled: 1})
            })

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 220},
/*                
                {type: 'main', size: 400,
                    tabs: {
                        tabs:    [
                            {id: 'payment_acknowledgments', caption: 'Оплаченные квитанции (платежные документы)'},
                            {id: 'acknowledgment_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_acknowledgment_common
                    }
                },
*/                

            ],

            onRender: function (e) {
//                this.get ('main').tabs.click (data.active_tab)
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        $panel.w2reform ({

            name   : form_name,

            record : it,

            fields : [
                {name: 'accountnumber', type: 'text'},
                {name: 'customer_label', type: 'text'},
                {name: 'amount', type: 'float', options: {min: 0, precision: 2}},
                {name: 'pay_amount', type: 'float', options: {min: 0, precision: 2}},
            ],

            onRefresh: function (e) {e.done (function () {

            })}

        })

        $_F5 (data)

    }

})