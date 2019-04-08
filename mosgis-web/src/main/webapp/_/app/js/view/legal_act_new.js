define ([], function () {

    var name = 'legal_act_new_form'

    return function (data, view) {

        function recalc(){
            var form = w2ui[name]
            var r = form.record
            var scope = r.scope? r.scope.id : undefined
            var data = $('body').data('data')
            $('div[data-block-name=legal_act_new] input[name=oktmo]').closest('div.w2ui-field').toggle(scope == 1)

            var hidden = 0

            if (!scope) {
                hidden++
            }

            var o = {
                form: 343,
                page: 265,
                box: 356,
                popup: 388,
                'form-box': 343,
            }

            for (var k in o)
                $('input[name=level_]').closest('.w2ui-' + k).height(o [k] - 31 * hidden)
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [
                    {name: 'level_', type: 'list', options: {
                        items: data.vc_legal_act_levels.items.filter((i) => { return i.id != 3 })}
                    },
                    {name: 'code_vc_nsi_324', type: 'list', options: {items: []}},
                    {name: 'name', type: 'text'},
                    {name: 'docnumber', type: 'text'},
                    {name: 'approvedate', type: 'date'},
                    {name: 'files', type: 'file', options: {max: 1}},
                    {name: 'scope', type: 'list', options: {items: [
                        {id: 0, text: 'город Москва'},
                        {id: 1, text: 'выбранные муниципальные образования'},
                    ]}},
                    {name: 'oktmo', type: 'enum', hint: 'ОКТМО', options: {
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

                focus: -1,

                onChange: function (e) {

                    var form = this

                    if (e.target == 'scope') {
                        e.done (recalc)
                    }
                    if (e.target == 'level_') {
                        e.done(function(){
                            var data = $('body').data('data')
                            var r = form.record
                            form.get('code_vc_nsi_324').options.items = data.vc_nsi_324.items.filter(function (i) {
                                return i.level_ == r.level_.id
                            })
                            form.refresh()
                        })
                    }
                },

                onRefresh: function(e) {
                    e.done(recalc)
                },
            })

       })

    }

})