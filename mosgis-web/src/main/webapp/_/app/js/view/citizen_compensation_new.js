define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_member_person_new_form',

                record: data.record,

                fields : [
                    {name: 'uuid_person', type: 'list', options: {
                        url: '/_back/?type=vc_persons',
                        filter: false,
                        cacheMax: 50,
                        onRequest: function(e) {
                            e.postData = {
                                search: [
                                    {field: 'label_uc', operator: 'contains', value: e.postData.search}
                                ],
                                searchLogic: 'OR',
                                offset: 0,
                                limit: 50
                            }
                        },
                        onLoad: function (e) {
                            e.data = {
                                status: "success", 
                                records: e.data.content.root.map (function (i) {return {
                                    id: i.id, 
                                    text: i.label + ' ' + [
                                        {label: 'СНИЛС', value: i.snils},
                                        {label: 'д.р.', value: dt_dmy(i.birthdate)}
                                    ].filter((i) => !!i.value)
                                        .map((i) => ((i.label? (i.label + ': ') : '') + i.value))
                                        .join(' ')
                                }})
                            }
                        }
                    }},
                    {name: 'fiashouseguid', type: 'list', options: {
                        url: '/_back/?type=voc_building_addresses',
                        filter: false,
                        cacheMax: 50,
                        onLoad: function (e) {
                            e.data = {
                                status: "success", 
                                records: e.data.content.vc_buildings.map (function (i) {return {
                                    id: i.id, 
                                    text: i.postalcode + ', ' + i.label
                                }})
                            }
                        }
                    }},
                    {name: 'registrationtype', type: 'list', options: {items: data.vc_addr_reg_types.items}},
                    {name: 'apartmentnumber', type: 'text'},
                    {name: 'flatnumber', type: 'text'},

                ],

                focus: 0

            })

       })

    }

})