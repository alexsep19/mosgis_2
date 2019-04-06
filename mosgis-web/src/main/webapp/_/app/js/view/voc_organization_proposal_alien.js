define ([], function () {

    return function (data, view) {

        var form_name = 'voc_organization_proposal_alien_form'

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            data.item.vc_organization_types_label = data.item ['vc_organization_types.label']

            var r = clone(data.item)

            data.fiashouseguid = r.fiashouseguid

            r.uuid_org_parent = r.parent

            r.label_org_parent = r["parent_org.label"]

            w2ui [form_name].record = r

            $('div[data-block-name=voc_organization_proposal_alien] input, textarea').prop({disabled: data.__read_only})

            w2ui [form_name].refresh()

        }

        data.selected = {
            address: {
                id: data.item.fiashouseguid,
                text: data.item ["vc_build_address.label"]
            },
            registrationcountry: {
                id: data.item.registrationcountry,
                text: [data.item ["vc_oksm.alfa2"], data.item ["vc_oksm.fullname"]].join(", ")
            }
        }

        var it = data.item

        data.__read_only = true

        $('title').text (it.label)

        fill (view, it, $('#body'))

        $('#main_container').w2reform({
            name: form_name,
            record: it,
            fields: [
                {name: 'vc_organization_types_label', type: 'text'},

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
                        selected: data.selected.address,
                        items: [data.selected.address],
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
                        selected: data.selected.registrationcountry,
                        items: [data.selected.registrationcountry],
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
        })

        $('#container').w2relayout({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'voc_organization_proposal_log', caption: 'История'},
                        ].filter(not_off),

                        onClick: $_DO.choose_tab_voc_organization_proposal_alien

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'voc_organization_proposal_alien.active_tab')
            },

        });

        $_F5(data)
    }

})