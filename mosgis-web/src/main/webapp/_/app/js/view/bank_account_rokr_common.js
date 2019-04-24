define ([], function () {

    var form_name = 'bank_account_rokr_common_form'

    
    return function (data, view) {
    
        $_F5 = function (data) {

            var it = data.item

            data.item.__read_only = data.__read_only

            $('body').data('__read_only', data.__read_only)

            data.item.label_org = data.item['org.label']

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=bank_account_rokr_common] input').prop({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 220},
                {type: 'main', size: 300,
                    tabs: {
                        tabs:    [
                            {id: 'bank_account_rokr_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_bank_account_rokr_common
                    }
                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        var it = data.item

        fill (view, it, $panel)

        var bank = {
            id: it ['bank.bic'],
            text: it ['bank.namep'],
        }

        var now = dt_dmy (new Date ().toJSON ())

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'accountnumber', type: 'text'},
                {name: 'opendate', type: 'date', options: {end: now}},
                {name: 'label_cred_org', type: 'text'},
                {name: 'uuid_cred_org', type: 'hidden'},
                {name: 'bikcredorg', type: 'list', hint: 'Адрес', options: {

                        url: '/_back/?type=voc_bic',

                        filter: false,

                        cacheMax: 50,
                        selected: bank,
                        items: [bank],

                        postData: {offset: 0, limit: 50},

                        onSearch: function (e) {
                            this.options.postData.search = [{'value': e.search}]
                            this.options.postData.searchLogic = "OR"
                        },

                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.bic + ' ' + i.namep
                                    }
                                })
                            }
                        }

                }},
            ],
            
            onRefresh: function (e) {e.done (function () {
                clickOff ($('#label_cred_org'))
                clickOn ($('#label_cred_org'), $_DO.open_orgs_bank_account_rokr_common)

                var r = w2ui[form_name].record
                $('input[name=opendate]').prop('readonly', !!r.accountregoperatorguid)
            })}
        })

        $_F5 (data)
    }

})