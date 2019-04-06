define([], function () {

    return function (data, view) {

        $(fill(view, data.record)).w2uppop({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform({

                name: 'voc_organization_proposal_alien_add_new_form',

                record: data.record,

                fields: [
                    {name: 'id_type', type: 'hidden'},
                    {name: 'fullname', type: 'text'},
                    {name: 'shortname', type: 'text'},
                    {name: 'nza', type: 'text'},
                    {name: 'accreditationstartdate', type: 'date'},
                    {name: 'inn', type: 'text'},
                    {name: 'kpp', type: 'text'},
                    {name: 'address', type: 'text'},
                    {name: 'fiashouseguid', type: 'list', hint: 'Глобальный уникальный идентификатор дома по ФИАС', options: {
                            url: '/_back/?type=voc_building_addresses',
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
                    {name: 'registrationcountry', type: 'list', hint: 'Страна', options: {
                        url: '/_back/?type=voc_oksm',
                                filter: false,
                                cacheMax: 50,
                                onLoad: function (e) {
                                e.data = {
                                    status: "success",
                                    records: e.data.content.vc_oksm.map(function (i) {
                                        return {
                                            id: i.id,
                                            text: i.alfa2 + ', ' + i.label
                                        }
                                    })
                                    }
                                }
                        },
                    },
                    {name: 'accreditationenddate', type: 'date'}
                ],

                onRefresh: function (e) {

                    e.done(function () {

                        clickOn($('#label_org_parent'), $_DO.open_orgs_voc_organization_proposal_alien_add_new)

                    })
                }
            })

        })

    }

})