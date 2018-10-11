define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                
                    {name: 'startdate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.date_),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.date_),
                    }},
                    
                    
                    {name: 'id_reason', type: 'list', options: {items: data.vc_charter_object_reasons.items}},
                    {name: 'ismanagedbycontract', type: 'list', options: {items: [
                        {id: 0, text: 'без договора'},
                        {id: 1, text: 'по договору управления'},
                    ]}},

                    {name: 'fiashouseguid', type: 'list', options: {
                        url: '/mosgis/_rest/?type=voc_building_addresses',
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
                ],
                
            })

       })

    }

})