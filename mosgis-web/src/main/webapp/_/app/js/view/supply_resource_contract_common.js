define ([], function () {

    var form_name = 'supply_resource_contract_common_form'

    function recalc () {

        var f = w2ui [form_name]

        var v = f.values ()
        var onetimepayment = v.onetimepayment > 0
        var it = $('body').data('data').item
        var is_customer_executor = it.id_customer_type == 5 // вторая сторона договора исполнитель КУ

        var is_on = {
            '#tr_ddt_m_start' : v.volumedepends == 1 || v.mdinfo == 1,
            '#tr_ddt_m_end'   : v.volumedepends == 1 || v.mdinfo == 1,
            '#tr_ddt_d_start' : !onetimepayment && (!is_customer_executor || v.mdinfo >= 0),
            '#tr_ddt_i_start' : !onetimepayment && !is_customer_executor && v.is_contract != 1,
            '#tr_ddt_n_start' : is_customer_executor && v.countingresource == 1 && v.is_contract != 1,
            '#tr_accrualprocedure': is_customer_executor
        }

        var hidden = 0
        for (var s in is_on) {
            $(s).css({display: is_on [s]? 'table-row' : 'none'})
            hidden = hidden + (is_on [s] ? 0 : 1)
        }
        var is_on_ddt_header = hidden != Object.keys(is_on).length
        $('#tr_ddt_header').css({display: is_on_ddt_header ? 'table-row' : 'none'})

        var s = 680 - hidden * 30 - (is_on_ddt_header? 0 : 60)
        var l = w2ui ['passport_layout']
        var t = l.get ('top')
        if (t.size != s) l.set ('top', {size: s})

        var is_active_input = {
            'input[name=plannedvolumetype]': v.isplannedvolume == 1,
            'input[name=onetimepayment]' : !is_customer_executor,
            'input[name=volumedepends]': !is_customer_executor && !onetimepayment,

            'input[name=accrualprocedure]': is_customer_executor,
            'input[name=countingresource]': is_customer_executor,
            'input[name=mdinfo]': is_customer_executor && v.countingresource == 1
        }

        for (var s in is_active_input) {
            $(s).closest('td').toggle(is_active_input [s])
        }
        
        if (it._can.set_bank_acct) {

            $('#uuid_bnk_acct_div *')
                .css  ({cursor: 'pointer'})
                .attr ({title: 'Сменить платёжные реквизиты для данного договора'})

            clickOff ($('#uuid_bnk_acct_div input'))
            clickOn ($('#uuid_bnk_acct_div input'), $_DO.set_bank_acct_supply_resource_contract_common)

        }                
        
    }

    return function (data, view) {

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=supply_resource_contract_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 640},
                {type: 'main', size: 200,
                    tabs: {
                        tabs:    [
//                            {id: 'supply_resource_contract_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_supply_resource_contract_common
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
        var last = {id: 99, text: 'последнее число'}
        ddt_30.push (last)
        var ddt_30_from = ddt_30.map (function (i) {return {id: i.id, text: i.text.replace ('е число', 'го числа')}})


        data.voc_bool = [
            {id: "0", text: "Нет"},
            {id: "1", text: "Да"}
        ]

        data.voc_specifyingqualityindicators = [
            {id: "0", text: "Исполнитель коммунальных услуг"},
            {id: "1", text: "РСО"}
        ]

        var nxt = [
            {id: "0", text: "текущего месяца"},
            {id: "1", text: "следующего месяца"},
        ]

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'org.label', type: 'text'},
                {name: 'contractnumber', type: 'text'},

                {name: 'signingdate', type: 'date'},
                {name: 'effectivedate', type: 'date'},

                {name: 'completiondate', type: 'date'},
                {name: 'autorollover', type: 'list', options: {items: data.voc_bool}},

                {name: 'code_vc_nsi_58', type: 'list', options: {items: data.vc_nsi_58.items}},

                {name: 'is_contract', type: 'list', options: {items: data.voc_bool}},
                {name: 'onetimepayment', type: 'list', options: {items: data.voc_bool}},

                {name: 'isplannedvolume', type: 'list', options: {items: data.voc_bool}},
                {name: 'plannedvolumetype', type: 'list', options: {items: data.vc_gis_ctr_dims.items}},

                {name: 'accrualprocedure', type: 'list', options: {items: data.vc_gis_ctr_dims.items}},

                {name: 'countingresource', type: 'list', options: {items: data.voc_specifyingqualityindicators}},
                {name: 'mdinfo', type: 'list', options: {items: data.voc_bool}},

                {name: 'specqtyinds', type: 'list', options: {items: data.vc_gis_ctr_dims.items}},

                {name: 'volumedepends', type: 'list', options: {items: data.voc_bool}},

                {name: 'ddt_m_start', type: 'list', options: {items: ddt_30_from}},
                {name: 'ddt_m_end',   type: 'list', options: {items: ddt_30}},
                {name: 'ddt_m_start_nxt', type: 'list', options: {items: nxt}},
                {name: 'ddt_m_end_nxt',   type: 'list', options: {items: nxt}},

                {name: 'ddt_d_start', type: 'list', options: {items: ddt_30_from}},
                {name: 'ddt_d_start_nxt', type: 'list', options: {items: nxt}},

                {name: 'ddt_i_start', type: 'list', options: {items: ddt_30_from}},
                {name: 'ddt_i_start_nxt', type: 'list', options: {items: nxt}},

                {name: 'ddt_n_start', type: 'list', options: {items: ddt_30_from}},
                {name: 'ddt_n_start_nxt', type: 'list', options: {items: nxt}},

                {name: 'uuid_bnk_acct', type: 'list', options: {items: data.bnk_accts_actual || []}},
                
            ],

            focus: -1,

            onRefresh: function (e) {e.done (recalc)},
            onChange:  function (e) {if (e.target='onetimepayment') e.done (recalc)},

        })

        $_F5 (data)
    }

})