define ([], function () {

    var name = 'voc_user_form'

    function recalc () {

        var v = w2ui [name].values ()
        
        var on = v.id_reason - 1
        
        var $protocol = $('.protocol')
        
        if (on) {
            $protocol.show ()
            if (!v.files) $('.file-input').click ()
        }
        else {
            $protocol.hide ()
        }
        
        var o = {
            form:  205,
            page:  125,
            box:   225,
            popup: 260,
            'form-box': 210,
        }
        
        for (var k in o) $protocol.closest ('.w2ui-' + k).height (o [k] + 85 * on)

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

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
                    
                    {name: 'description',  type: 'textarea' },
                    {name: 'files', type: 'file', options: {max: 1}},                    
                    
                ],
                
                onChange: function (e) {if (e.target == "id_reason") e.done (recalc)},
                        
                onRender: function (e) {e.done (setTimeout (recalc, 100))}                
                
            })

       })

    }

})