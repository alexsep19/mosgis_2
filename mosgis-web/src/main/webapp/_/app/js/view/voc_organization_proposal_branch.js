define ([], function () {

    return function (data, view) {

        var form_name = 'voc_organization_proposal_branch_form'

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            data.item.vc_organization_types_label = data.item ['vc_organization_types.label']

            var r = clone(data.item)

            data.fiashouseguid = r.fiashouseguid

            r.uuid_org_parent = r.parent

            r.label_org_parent = r["parent_org.label"]

            w2ui [form_name].record = r

            $('div[data-block-name=voc_organization_proposal_branch] input, textarea').prop({disabled: data.__read_only})

            w2ui [form_name].refresh()

        }

        data.selected_address = {
            id: data.item.fiashouseguid,
            text: data.item ["vc_build_address.label"]
        }

        var it = data.item

        data.__read_only = true

        $('title').text (it.label)

        fill (view, it, $('#body'))

        $('#main_container').w2reform({
            name: form_name,
            record: it,
            fields: [
                {name: 'id_type', type: 'hidden'},
                {name: 'vc_organization_types_label', type: 'text'},

                {name: 'uuid_org_parent', type: 'hidden'},
                {name: 'label_org_parent', type: 'text'},

                {name: 'address', type: 'text'},
                {name: 'fiashouseguid', type: 'list', hint: 'Глобальный уникальный идентификатор дома по ФИАС'
                    , options: {
                        url: '/_back/?type=voc_building_addresses',
                        filter: false,
                        selected: data.selected_address,
                        items: [data.selected_address],
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
                {name: 'fullname', type: 'text'},
                {name: 'shortname', type: 'text'},
                {name: 'ogrn', type: 'text'},
                {name: 'stateregistrationdate', type: 'date'},
                {name: 'activityenddate', type: 'date'},
                {name: 'inn', type: 'text'},
                {name: 'kpp', type: 'text'},
                {name: 'okopf', type: 'text'},
                {name: 'info_source', type: 'text'},
                {name: 'dt_info_source', type: 'date'}
            ],
        })

        $('#container').w2relayout({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'voc_organization_proposal_log', caption: 'История'},
                        ].filter(not_off),

                        onClick: $_DO.choose_tab_voc_organization_proposal_branch

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'voc_organization_proposal_branch.active_tab')
            },

        });

        $_F5(data)
    }

})