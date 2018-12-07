define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_organization_branch_add_new_form',

                record: data.record,

                focus: 2,

                fields : [
                    {name: 'id_type', type: 'hidden'},
                    {name: 'uuid_org_parent', type: 'hidden'},
                    {name: 'label_org_parent', type: 'text'},
                    {name: 'fullname', type: 'text'},
                    {name: 'shortname', type: 'text'},
                    {name: 'ogrn', type: 'text'},
                    {name: 'stateregistrationdate', type: 'date'},
                    {name: 'inn', type: 'text'},
                    {name: 'kpp', type: 'text'},
                    {name: 'okopf', type: 'text'},
                    {name: 'address', type: 'text'},
                    {name: 'fiashouseguid', type: 'list', hint: 'Глобальный уникальный идентификатор дома по ФИАС', options: {
                        url: '/mosgis/_rest/?type=voc_building_addresses',
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
                    {name: 'info_source', type: 'text'},
                    {name: 'dt_info_source', type: 'date'}
                ],

                onRefresh: function (e) {

                    e.done(function () {

                        clickOn($('#label_org_parent'), $_DO.open_orgs_voc_organization_branch_add_new)

                    })
                }
            })

       })

    }

})