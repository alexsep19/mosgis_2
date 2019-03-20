define ([], function () {

    var form_name = 'legal_act_common_form'

    return function (data, view) {

        function recalc () {

            var form = w2ui[form_name]
            var r = form.record
            var scope = r.scope ? r.scope.id : undefined

            $('#oktmo').toggle(!!scope)

            if (scope) {
                // HACK: fix enum selected
                $(form.get('oktmo').el).data('selected', form.get('oktmo').options.selected).change()
            }

            var hidden = 0;
            if (!scope) hidden++;

            var s = 320 - hidden * 31
            var l = w2ui ['passport_layout']
            var t = l.get('top')
            if (t.size != s)
                l.set('top', {size: s})
        }

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=legal_act_common] input, textarea').prop ({disabled: data.__read_only})

            f.refresh ()

            clickOn($('#file_label'), $_DO.download_legal_act_common)
        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [
                {type: 'top', size: 320},
                {type: 'main', size: 400,
                    tabs: {
                        tabs: [
                            {id: 'legal_act_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_legal_act_common
                    }
                }
            ],
            onRender: function (e) {
                this.get('main').tabs.click(data.active_tab)
            },
        });

        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'code_vc_nsi_324', type: 'list', options: {items: data.vc_nsi_324.items.filter(function (i) {
                        return i.level_ == data.item.level_
                    })
                }},
                {name: 'name', type: 'text'},
                {name: 'docnumber', type: 'text'},
                {name: 'approvedate', type: 'date'},
                {name: 'files', type: 'file', options: {max: 1}},
                {name: 'scope', type: 'list', options: {items: [
                    {id: 0, text: 'город Москва'},
                    {id: 1, text: 'выбранные муниципальные образования'},
                ]}},
                {name: 'oktmo', type: 'enum', hint: 'ОКТМО', options: {
                    items:  data.item.selected_oktmo,
                    selected: data.item.selected_oktmo,
                    renderItem: function(i, idx, remove) {
                        return i.code + remove
                    },
                    maxDropWidth: 800,
                    openOnFocus: true,
                    filter: false,
                    url: '/mosgis/_rest/?type=voc_oktmo',
                    cacheMax: 50,
                    postData: {
                        offset: 0,
                        limit: 50
                    },
                    onRequest: function (e) {
                        e.postData = {
                            search: [
                                {field: 'code', operator: 'contains', value: e.postData.search}
                            ],
                            searchLogic: 'AND'
                        }
                    },
                    onLoad: function (e) {
                        e.data = {
                            status: "success",
                            records: e.data.content.vc_oktmo.map(function (i) {
                                return {
                                    id: i.id,
                                    code: i.code,
                                    text: i.code + ' ' + i.site_name
                                }
                            })
                        }
                    }
                }},
            ],

            focus: -1,

            onRefresh: function (e) {
                e.done(recalc)
            },

            onChange: function (e) {
                if (e.target == 'scope') {
                    e.done(recalc)
                }
            },
        })

        $_F5 (data)

    }

})