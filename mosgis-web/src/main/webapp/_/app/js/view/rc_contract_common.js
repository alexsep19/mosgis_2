define ([], function () {

    var form_name = 'rc_contract_common_form'

    function recalc () {

        var f = w2ui [form_name]

        var v = f.values ()
        var data = $('body').data('data')
        var __read_only =  $('body').data('__read_only')
        var it = data.item

        var id_service_type = v.id_service_type? (v.id_service_type.id || v.id_service_type) : null
        var is_customer_oms = it['org_customer.is_oms'] || false

        var is_active_input = {
            'input[name=is_list_md]': {
                on: !it['org_customer.is_oms'],
                row: 'md'
            },
            'input[name=is_proc_md_ind]' : {
                on: !it['org_customer.is_oms'],
                row: 'md'
            },
            'input[name=is_all_house]': {
                on: !it['org_customer.is_oms']
            },
            'input[name=is_accounts]': {
                edit: id_service_type == 1 && !is_customer_oms,
                field: 'is_accounts',
                default: id_service_type == 1 || is_customer_oms ? 1 : 0,
            },
            'input[name=is_proc_pay]': {
                edit: id_service_type == 1 && !is_customer_oms,
                field: 'is_proc_pay',
                default: id_service_type == 1 || is_customer_oms ? 1 : 0
            },
        }

        var hidden = {}

        for (var s in is_active_input) {

            var i = is_active_input [s]

            if ('on' in i) {
                $(s).closest('td').toggle(i.on)
                if (!$(s).closest('tr').find('td:visible').length) {
                    hidden[i.row]++
                }
            }
            if ('edit' in i && !__read_only) {
                $(s).prop('disabled', !i.edit)
            }
            if ('default' in i && i.field) {
                var field = i.field
                if (f.get(field).options.items) {
                    f.record[field] = f.get(field).options.items.find(j => j.id == i.default)
                }
            }
        }

        var s = 410 - Object.keys(hidden).length * 30
        var l = w2ui ['passport_layout']
        var t = l.get('top')
        if (t.size != s)
            l.set('top', {size: s})

        clickOn($('#label_org'), $_DO.open_orgs_rc_contract_common)
    }

    return function (data, view) {

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            $('body').data('__read_only', data.__read_only)

            data.item.label_org = data.item['org.label']

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=rc_contract_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 400},
                {type: 'main', size: 200,
                    tabs: {
                        tabs:    [
                            {id: 'rc_contract_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_rc_contract_common
                    }
                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        var ddt_30 = [];
        for (var i = 1; i <= 30; i ++) ddt_30.push ({id: i, text: i + '-е число'});
        var ddt_31 = ddt_30.concat ([{id: 31, text: '31-е число'}]);
        var last = {id: 99, text: 'последнее число'}
        ddt_30.push (last)
        ddt_31.push (last)
        var ddt_31_from = ddt_31.map (function (i) {return {id: i.id, text: i.text.replace ('е число', 'го числа')}})

        data.voc_bool = [
            {id: "0", text: "Нет"},
            {id: "1", text: "Да"}
        ]

        var nxt = [
            {id: "0", text: "текущего месяца"},
            {id: "1", text: "следующего месяца"},
        ]

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'contractnumber', type: 'text'},
                {name: 'signingdate', type: 'date'},
                {name: 'effectivedate', type: 'date'},
                {name: 'completiondate', type: 'date'},
                {name: 'uuid_org', type: 'text', hidden: true},
                {name: 'label_org', type: 'text'},

                {name: 'id_service_type', type: 'list', options: {items: data.vc_rc_ctr_service_types.items}},

                {name: 'is_accounts', type: 'list', options: {items: data.voc_bool}},
                {name: 'is_invoices', type: 'list', options: {items: data.voc_bool}},
                {name: 'is_proc_pay', type: 'list', options: {items: data.voc_bool}},
                {name: 'is_list_md', type: 'list', options: {items: data.voc_bool}},
                {name: 'is_proc_md_ind', type: 'list', options: {items: data.voc_bool}},
                {name: 'is_all_house', type: 'list', options: {items: data.voc_bool}},

                {name: 'ddt_d_start', type: 'list', options: {items: ddt_31_from}},
                {name: 'ddt_d_start_nxt', type: 'list', options: {items: nxt}},
            ],

            focus: -1,

            onRefresh: function (e) {e.done (recalc)},

            onChange:  function (e) {
                if (e.target == 'id_service_type') {
                    e.done (function(e){
                        recalc();
                        this.refresh()
                    })
                }
            }
        })

        $_F5 (data)
    }

})