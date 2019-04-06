define ([], function () {

    var form_name = 'premise_usage_tarif_common_form'

    return function (data, view) {

        function recalc () {
            var form = w2ui[form_name]
            // HACK: fix enum selected
            $(form.get('oktmo').el).data('selected', form.get('oktmo').options.selected).change()
        }

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=premise_usage_tarif_common] input, textarea').prop ({disabled: data.__read_only})

            f.refresh ()
        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [
                {type: 'top', size: 245},
                {type: 'main', size: 400,
                    tabs: {
                        tabs: [
                            {id: 'premise_usage_tarif_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_premise_usage_tarif_common
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
                {name: 'name', type: 'textarea'},
                {name: 'datefrom', type: 'date'},
                {name: 'dateto', type: 'date'},
                {name: 'price', type: 'float', options: {min: 0}},
                {name: 'oktmo', type: 'enum', hint: 'ОКТМО', options: {
                    items:  data.item.selected_oktmo,
                    selected: data.item.selected_oktmo,
                    maxDropWidth: 800,
                    renderItem: function (i, idx, remove) {
                        return i.code + remove
                    },
                    url: '/_back/?type=voc_oktmo',
                    openOnFocus: true,
                    filter: false,
                    cacheMax: 50,
                    postData: {
                        offset: 0,
                        limit: 50
                    },
                    onRequest: function(e) {
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

            focus: 0,

            onRefresh: function (e) {
                e.done(recalc)
            },
        })

        $_F5 (data)

    }

})