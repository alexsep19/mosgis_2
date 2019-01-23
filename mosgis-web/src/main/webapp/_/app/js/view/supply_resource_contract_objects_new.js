define ([], function () {

    var form_name = 'supply_resource_contract_objects_new_form'

    function recalc() {

        var f = w2ui [form_name]

        var v = f.values()
        var it = $('body').data('data').item

        var is_on = {
            'input[name=uuid_premise]': [1, 4].indexOf(it.id_customer_type) != -1,
        }

        var hidden = 0
        for (var s in is_on) {
            $(s).closest('td').toggle(is_on [s])
            hidden = hidden + (is_on [s] ? 0 : 1)
        }

        var o = {
            form: 145,
            page: 85,
            box: 156,
            popup: 190,
            'form-box': 143,
        }

        for (var k in o)
            $('input[name=uuid_premise]').closest('.w2ui-' + k).height(o [k] - 30 * hidden)
    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'fiashouseguid', type: 'list', hint: 'Адрес', options: {
                        url: '/mosgis/_rest/?type=supply_resource_contract_objects&part=buildings',
                        filter: false,
                        cacheMax: 50,
                        postData: {
                            offset: 0,
                            limit: 50,
                            is_condo: [1, 2, 3].indexOf(data.item.id_customer_type) != -1? '1'
                                : 4 == data.item.id_customer_type? '0'
                                : undefined
                        },
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: (i.is_condo === 1 ? 'МКД ' : i.is_condo === 0 ? 'ЖД ' : '') + i.label,
                                        uuid_house: i.uuid_house
                                    }
                                })
                            }
                        }
                    }},
                    {name: 'uuid_premise', type: 'list', hidden:[1, 4].indexOf(data.item.id_customer_type) == -1
                        , options: {items: data.premises}
                    },
                ],

                focus: 0,

                onRefresh: function(e) { e.done(recalc) },

                onChange: function (e) {

                    var form = this

                    if (e.target == 'fiashouseguid' ) {

                        var uuid_house = e.value_new.uuid_house

                        e.done(function(){
                            query({type: 'premises', id: undefined}, {data: {uuid_house: uuid_house || '00'}}, function (d) {

                                var f = form.get('uuid_premise')

                                f.options.items = d.vw_premises.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.label
                                    }
                                })

                                delete form.record.uuid_premise

                                $().w2overlay(); // HACK: lost focus, hide dropdown on Enter

                                form.refresh()
                            })
                        })
                    }
                }

            })

       })

    }

})