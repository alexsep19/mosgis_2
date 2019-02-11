define ([], function () {

    return function (data, view) {

        var form_name = 'settlement_doc_payments_popup_form';

        function recalc(){
            var r = w2ui[form_name].record

            var id_type = r.id? r.id_type : ($_USER.has_nsi_20(2)? 1 : 0)

            var is_on = {
                'input[name=credited]': id_type == 1,
                'input[name=receipt]': id_type == 1,
                'input[name=debts]': id_type == 1,
                'input[name=overpayment]': id_type == 1
            }

            var hidden = 0
            for (var s in is_on) {
                $(s).closest('div.w2ui-field').toggle(is_on [s])
                hidden = hidden + (is_on [s] ? 0 : 1)
            }

            var $row = $('input[name=credited]')
            var o = {
                form: 263,
                page: 274,
                box: 276,
                popup: 308,
                'form-box': 263,
            }

            for (var k in o)
                $row.closest('.w2ui-' + k).height(o [k] - 30 * hidden)
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [                                
                
                    {name: 'month', type: 'list', options: {items: data.months}},
                    {name: 'year',   type: 'text'},                                        
                    {name: 'credited', type: 'float', options: {precision: 2}},
                    {name: 'receipt', type: 'float', options: {precision: 2}},
                    {name: 'debts', type: 'float', options: {precision: 2}},
                    {name: 'overpayment', type: 'float', options: {precision: 2}},
                    {name: 'paid', type: 'float', options: {precision: 2}},

                ],
                
                focus: data.record.id? 2 : 0,

                onRender: function (e) {
                    e.done(setTimeout(recalc, 100))
                }
            })

       })

    }

})