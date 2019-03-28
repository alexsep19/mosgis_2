define ([], function () {

    return function (data, view) {
        var f = 'tarif_diff_form'

        function recalc(){
            var form = w2ui[f]
            var r = form.record
            var id_type = r.id_type || r['type.id']
            var operator = r.operator? r.operator.id : undefined
            var data = clone($('body').data('data'))

            var is_on = {
                '#tr_valuereal'       : id_type == 'Real',
                '#tr_valueinteger'    : id_type == 'Integer',
                '#tr_valueboolean'    : id_type == 'Boolean',
                '#tr_valuestring'     : id_type == 'String',
                '#tr_label'           : id_type == 'Multiline',
                '#tr_valuemultiline'  : id_type == 'Multiline',
                '#tr_valuedate'       : id_type == 'Date',
                '#tr_valueyear'       : id_type == 'Year',
                '#tr_fias'            : id_type == 'FIAS',
                '#tr_oktmo'           : id_type == 'OKTMO',
                '#tr_enumeration'     : id_type == 'Enumeration',
                '#tr_operator'        : ['Real', 'Integer', 'Enumeration', 'Date', 'Year'].indexOf(id_type) != -1,
                '.from,.to': /Range/.test(operator),
            }

            for (s in is_on) {
                $(s).toggle(!!is_on[s])
            }

            // HACK: fix enum selected
            $(form.get('fias').el).data('selected', form.get('fias').options.selected).change()
            $(form.get('oktmo').el).data('selected', form.get('oktmo').options.selected).change()
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form input').prop({disabled: !data._can.update})

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'uuid_tf', type: 'text'},
                    {name: 'id_type', type: 'text'},
                    {name: 'code_diff', type: 'list', options: {
                        items: data.vc_diff.items
                    }},
                    {name: 'operator', type: 'list', options: {
                            items: data.vc_diff_value_ops.items.filter((i) => {
                                switch(data.record.id_type) {
                                    case 'Enumeration':
                                        i.text = i.text.replace('Диапазон значений', 'Включая значения')
                                        i.text = i.text.replace('Исключая диапазон значений', 'Исключая значения')
                                        return /Range/.test(i.id)
                                    default: return true
                                }
                            })
                        }
                    },
                    {name: 'valuestring', type: 'text'},

                    {name: 'label', type: 'text'},
                    {name: 'valuemultiline', type: 'textarea'},

                    {name: 'valuereal', type: 'float', options: {min: 0, autoFormat: false}},
                    {name: 'valuereal_to', type: 'float', options: {min: 0, autoFormat: false}},

                    {name: 'valueinteger', type: 'int', options: {min: 0, autoFormat: false}},
                    {name: 'valueinteger_to', type: 'int', options: {min: 0, autoFormat: false}},

                    {name: 'valuedate', type: 'date'},
                    {name: 'valuedate_to', type: 'date'},

                    {name: 'valueyear', type: 'int', options: {min: 0, autoFormat: false}},
                    {name: 'valueyear_to', type: 'int', options: {min: 0, autoFormat: false}},

                    {name: 'valueboolean', type: 'list', options: {
                        items: [
                            {id: 0, text: 'Нет'},
                            {id: 1, text: 'Да'}
                        ]
                    }},
                    {name: 'fias', type: 'enum', options: {
                        url: '/mosgis/_rest/?type=voc_building_addresses',
                        items:  data.record.fias,
                        selected: data.record.fias,
                        openOnFocus: true,
                        filter: false,
                        cacheMax: 50,
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.vc_buildings.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.postalcode + ', ' + i.label
                                    }
                                })
                            }
                        }
                    }},
                    {name: 'oktmo', type: 'enum', hint: 'ОКТМО', options: {
                        items:  data.record.oktmo,
                        selected: data.record.oktmo,
                        maxDropWidth: 800,
                        renderItem: function (i, idx, remove) {
                            return '<span title="' + i.text + '" >' + i.code + '</span>' + remove
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
                    {name: 'enumeration', type: 'enum', options: {
                        url: '/mosgis/_rest/?type=tarif_diffs&part=enumeration',
                        items:  data.record.enumeration,
                        selected: data.record.enumeration,
//                        renderItem: function (i, idx, remove) {
//                            return '<span title="' + i.text + '" >' + i.text + '</span>' + remove
//                        },
                        openOnFocus: true,
                        filter: false,
                        cacheMax: 50,
                        postData: {data: {
                            registrynumber: data.record.registrynumber
                        }},
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.label
                                    }
                                })
                            }
                        }
                    }},
                ],

                focus: 0,

                onChange: function(e) {
                    if (e.target == 'operator') {
                        e.done(recalc)
                    }
                },
                onRefresh: function(e) {
                    e.done(recalc)
                }
            })

       })

    }

})