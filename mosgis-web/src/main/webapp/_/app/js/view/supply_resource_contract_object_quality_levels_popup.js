define ([], function () {

    return function (data, view) {
        var f = 'supply_resource_contract_object_quality_level_form'

        var is_update = data.record.id

        function recalc(){

            var r = w2ui[f].record
            var is_num_type = {1: true, 2: true}

            var is_on = {
               '#f_indicatorvalue_from': !r.code_vc_nsi_276 || r.code_vc_nsi_276.id_type == 1,
               '#f_indicatorvalue'     : r.code_vc_nsi_276 && r.code_vc_nsi_276.id_type == 2,
               '#f_indicatorvalue_is'  : r.code_vc_nsi_276 && r.code_vc_nsi_276.id_type == 3,
               '#f_indicatorvalue_okei': !r.code_vc_nsi_276 || r.code_vc_nsi_276 && is_num_type[r.code_vc_nsi_276.id_type]
            }

            for (s in is_on) {
                $(s).toggle(!!is_on[s])
            }

            if (r.code_vc_nsi_276 && r.code_vc_nsi_276.text) {
                $('#f_code_vc_nsi_276>div').attr('title', r.code_vc_nsi_276.text)
            }
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form input').prop({disabled: !data._can.update})

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'code_vc_nsi_276', type: 'list', options: {
                        items: data.vc_nsi_276.items
                    }},
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

                focus: is_update? -1 : 1,

                onChange: function(e) {
                    if (e.target == 'code_vc_nsi_276') {
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