define ([], function () {

    var name = 'social_norm_tarif_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [
                    {name: 'name', type: 'textarea'},
                    {name: 'datefrom', type: 'date'},
                    {name: 'dateto', type: 'date'},
                    {name: 'price', type: 'float', options: {min: 0}},
                    {name: 'unit', type: 'list', options: {items: data.vc_okei.items}},
                    {name: 'oktmo', type: 'enum', hint: 'ОКТМО', options: {
                        maxDropWidth: 800,
                        renderItem: function (i, idx, remove) {
                            return i.code + remove
                        },
                        url: '/mosgis/_rest/?type=voc_oktmo',
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

                focus: 0
            })

       })

    }

})