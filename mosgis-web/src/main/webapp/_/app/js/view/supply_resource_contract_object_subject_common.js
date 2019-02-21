define ([], function () {

    var form_name = 'supply_resource_contract_object_subject_common_form'

    function recalc () {

        var f = w2ui [form_name]

        var v = f.values ()
        var it = $('body').data('data').item

        var is_volume = it['sr_ctr.isplannedvolume'] && it['sr_ctr.plannedvolumetype'] == 20
        var is_on = {
            '#tr_volume'     : is_volume,
            '#tr_feedingmode': is_volume,
        }

        var hidden = 0
        for (var s in is_on) {
            $(s).css({display: is_on [s]? 'table-row' : 'none'})
            hidden = hidden + (is_on [s] ? 0 : 1)
        }

        var s = 220 - hidden * 30
        var l = w2ui ['passport_layout']
        var t = l.get ('top')
        if (t.size != s) l.set ('top', {size: s})

        recalc_ms_change()
    }

    function recalc_ms_change () {
        var data = $('body').data('data')
        var r = w2ui[form_name].record
        var service  = r.code_vc_nsi_3.id
        var resource = r.code_vc_nsi_239? r.code_vc_nsi_239.id : undefined

        w2ui[form_name].get('code_vc_nsi_239').options.items = data.vc_nsi_239.items.filter(function (i) {
            return data.service2resource[service] && data.service2resource[service][i.id]
        })

        var unit = data.service2resource[service]? data.service2resource[service][resource] : undefined
        w2ui[form_name].get('unit').options.items = unit || []
    }

    return function (data, view) {

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=supply_resource_contract_object_subject_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 220},
                {type: 'main', size: 200,
                    tabs: {
                        tabs: [
                            {
                                id: 'supply_resource_contract_subject_quality_levels',
                                caption: 'Показатели качества коммунальных ресурсов',
                                off: data.item['sr_ctr.specqtyinds'] != 20
                            },
                            {
                                id: 'supply_resource_contract_subject_other_quality_levels',
                                caption: 'Иные показатели качества коммунальных ресурсов',
                                off: data.item['sr_ctr.specqtyinds'] != 20
                            },
                            {id: 'supply_resource_contract_object_subject_common_log', caption: 'История изменений'},
                        ].filter(not_off),
                        onClick: $_DO.choose_tab_supply_resource_contract_object_subject_common
                    }
                },

            ],

            onRender: function (e) {
                var tabs = this.get('main').tabs
                tabs.click (tabs.get(data.active_tab)? data.active_tab : 'supply_resource_contract_object_subject_common_log')
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                {name: 'code_vc_nsi_239', type: 'list', options: {items: data.vc_nsi_239.items}},

                {name: 'startsupplydate', type: 'date', options:{
                    start: dt_dmy(data.item ['sr_ctr.effectivedate']),
                    end  : dt_dmy(data.item ['sr_ctr.completiondate'])
                }},
                {name: 'endsupplydate', type: 'date', options: {
                    start: dt_dmy(data.item ['sr_ctr.effectivedate']),
                    end  : dt_dmy(data.item ['sr_ctr.completiondate'])
                }},

                {name: 'volume', type: 'float'},
                {name: 'unit', type: 'list', options: {items: data.vc_okei.items}},

                {name: 'feedingmode', type: 'text'},

            ],

            focus: -1,

            onRefresh: function (e) {e.done (recalc)},

            onChange: function (e) {
                if (e.target == 'code_vc_nsi_3' && e.value_new.id) {
                    e.done(function(){
                        var r = w2ui[form_name].record
                        delete r.code_vc_nsi_239
                        delete r.unit
                        recalc_ms_change()
                        w2ui[form_name].refresh()
                    })
                }

                if (e.target == 'code_vc_nsi_239' && e.value_new.id) {
                    e.done(function () {
                        var r = w2ui[form_name].record
                        delete r.unit
                        recalc_ms_change()
                        w2ui[form_name].refresh()
                    })
                }
            }
        })

        $_F5 (data)
    }

})