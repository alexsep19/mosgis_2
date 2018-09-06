define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                
                    {name: 'startdate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.plandatecomptetion),
                    }},
                    {name: 'uuid_contract_agreement', type: 'list', options: {items: data.agreements}},
                    {name: 'fiashouseguid', type: 'list', options: {
                        url: '/mosgis/_rest/?type=voc_building_addresses',
                        filter: false,
                        cacheMax: 50,
                        onLoad: function (e) {
                            e.data = {
                                status: "success", 
                                records: e.data.content.vc_build_addresses.map (function (i) {return {
                                    id: i.id, 
                                    text: i.postalcode + ', ' + i.label
                                }})
                            }
                        }
                    }},
                ],
                
            })

       })

    }

})