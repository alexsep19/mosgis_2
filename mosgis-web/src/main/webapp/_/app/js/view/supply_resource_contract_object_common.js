define ([], function () {

    var form_name = 'supply_resource_contract_object_common_form'

    function recalc () {

        var f = w2ui [form_name]

        var v = f.values ()
        var it = $('body').data('data').item

        var is_on = {
            'input[name=uuid_premise]' : [1, 4].indexOf(it['sr_ctr.id_customer_type']) != -1,
        }

        var hidden = 0
        for (var s in is_on) {
            $(s).closest('td').toggle(is_on [s])
            hidden = hidden + (is_on [s] ? 0 : 1)
        }

        var s = 160 - hidden * 30
        var l = w2ui ['passport_layout']
        var t = l.get ('top')
        if (t.size != s) l.set ('top', {size: s})
    }

    return function (data, view) {

        var it = data.item

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=supply_resource_contract_object_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 160},
                {type: 'main', size: 200,
                    tabs: {
                        tabs: [
                            {
                                id: 'supply_resource_contract_object_subjects',
                                caption: 'Поставляемые ресурсы'
                            },
                            {id: 'supply_resource_contract_object_temperature_charts', caption: 'Температурный график'
                                , off: data.item['sr_ctr.specqtyinds'] != 20 || !data.is_on_tab_temperature
                            },
                            {id: 'supply_resource_contract_object_common_log', caption: 'История изменений'},
                        ].filter(not_off),
                        onClick: $_DO.choose_tab_supply_resource_contract_object_common
                    }
                },

            ],

            onRender: function (e) {
                var tabs = this.get('main').tabs
                tabs.click (tabs.get(data.active_tab)? data.active_tab : 'supply_resource_contract_object_common_log')
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, it, $panel)

        data.selected  = {
            fiashouseguid: {
                id: it.fiashouseguid,
                text: (it['house.is_condo'] === 1? 'МКД ' : it['house.is_condo'] === 0? 'ЖД ' : '') + it['building.label']
            },
            premise: {
                id: it['premise.id'],
                text: it['premise.label']
            }
        }

        $panel.w2reform ({

            name   : form_name,

            record : it,

            fields : [
                {name: 'fiashouseguid', type: 'list', hint: 'Адрес', options: {
                        url: '/mosgis/_rest/?type=supply_resource_contract_objects&part=buildings',
                        filter: false,
                        cacheMax: 50,
                        selected: data.selected.fiashouseguid,
                        items: [data.selected.fiashouseguid],
                        postData: {
                            offset: 0,
                            limit: 50,
                            is_condo: [1, 2, 3].indexOf(it['sr_ctr.id_customer_type']) != -1 ? '1'
                                    : 4 == it['sr_ctr.id_customer_type'] ? '0'
                                    : undefined
                        },
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: (i.is_condo === 1? 'МКД ' : i.is_condo === 0? 'ЖД ' : '') + i.label,
                                        uuid_house: i.uuid_house || '00'
                                    }
                                })
                            }
                        }
                    }},
                {name: 'uuid_premise', type: 'list'
                    , options: {
                        selected: data.selected.premise,
                        items: [data.selected.premise],
                        items: data.premises
                    }
                },
            ],

            focus: -1,

            onRefresh: function(e) { e.done(recalc) },

            onChange: function (e) {

                var form = this

                if (e.target == 'fiashouseguid') {

                    var uuid_house = e.value_new.uuid_house

                    e.done(function () {
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

        $_F5 (data)
    }

})