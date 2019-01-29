define ([], function () {

    var form_name = 'rc_contract_objects_popup_form'

    return function (data, view) {

        data.selected_address = {
            id: data.record.fiashouseguid,
            text: data.record ["building.label"]
        }

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'fiashouseguid', type: 'list', hint: 'Адрес', options: {
                        url: '/mosgis/_rest/?type=supply_resource_contract_objects&part=buildings',
                        filter: false,
                        cacheMax: 50,
                        selected: data.selected_address,
                        items: [data.selected_address],
                        postData: {
                            offset: 0,
                            limit: 50,
                            is_condo: [1, 2, 3].indexOf(data.item.id_customer_type) != -1? '1'
                                : 4 == data.item.id_customer_type? '0'
                                : undefined
                        },
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: (i.is_condo === 1 ? 'МКД ' : i.is_condo === 0 ? 'ЖД ' : '') + i.label,
                                        uuid_house: i.uuid_house
                                    }
                                })
                            }
                        }
                    }},
                    {name: 'dt_from', type: 'date'},
                    {name: 'dt_to', type: 'date'},
                    {name: 'id_ctr_status', type: 'list', options: {items: data.vc_gis_status.items}},

                ],

                focus: data.record.id? -1 : 0,
            })

       })

    }

})