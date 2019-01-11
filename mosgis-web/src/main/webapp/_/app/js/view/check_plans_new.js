define ([], function () {

    function recalc () {

        var $reg_num = $('#uriregistrationplannumber')
        var v = w2ui ['check_plan_form'].values ()

        if (!v.shouldberegistered) { $reg_num.prop ('disabled', true) }
        else { $reg_num.prop ('disabled', false) }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'check_plan_form',

                record: data.record,

                fields : [
                    {name: 'year', type: 'text'},
                    {name: 'shouldberegistered', type: 'list', options: {items: [{id:0, text: "Нет"}, {id:1, text: "Да"}]}},
                    {name: 'uriregistrationplannumber', type: 'text'}
                ],

                onChange: function (e) { if (e.target == 'shouldberegistered') { e.done (recalc) } },
                onRender: function (e) { e.done (setTimeout (recalc, 100)) }

            })

       })

    }

})