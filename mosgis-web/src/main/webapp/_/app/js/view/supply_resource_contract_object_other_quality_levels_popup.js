define ([], function () {

    return function (data, view) {
        var f = 'supply_resource_contract_object_other_quality_level_popup_form'

        var is_update = data.record.id

        function recalc(){

            var r = w2ui[f].record
            var is_num_type = {1: true, 2: true}
            var id_type = r.id_type.id

            var is_on = {
               '#f_indicatorvalue_from': !id_type || id_type == 1,
               '#f_indicatorvalue'     : id_type == 2,
               '#f_indicatorvalue_is'  : id_type == 3,
               '#f_indicatorvalue_okei': !id_type || is_num_type[id_type]
            }

            for (s in is_on) {
                $(s).toggle(!!is_on[s])
            }
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form input').prop({disabled: !data._can.update})

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'id_type', type: 'list', options: {
                        items: data.vc_gis_sr_ql_types.items
                    }},
                    {name: 'label', type: 'text'},
                    {name: 'indicatorvalue', type: 'float', options: {min: 0, autoFormat: true}, hidden: true},
                    {name: 'indicatorvalue_from', type: 'float', options: {min: 0, autoFormat: true}},
                    {name: 'indicatorvalue_to', type: 'float', options: {min: 0, autoFormat: true}},
                    {name: 'indicatorvalue_is', type: 'list', hidden: true, options: {
                        items: [
                            {id: 0, text: 'Не соответствует'},
                            {id: 1, text: 'Соответствует'}
                        ]
                    }},
                    {name: 'code_vc_okei', type: 'list', options: {items: data.vc_okei.items}},
                    {name: 'additionalinformation', type: 'text'},
                ],

                focus: 2,

                onChange: function(e) {
                    if (e.target == 'id_type') {
                        e.done(recalc)
                        var r = w2ui[f].record
                        delete r.indicatorvalue_is
                        delete r.indicatorvalue_from
                        delete r.indicatorvalue_to
                        delete r.indicatorvalue
                    }
                },
                onRefresh: function(e) {
                    e.done(recalc)
                }
            })

       })

    }

})