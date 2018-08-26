define ([], function () {

    return function (data, view) {

        var house = $_REQUEST.type == 'premise_residental' ? {is_condo: 1} : data.item
        
        var is_single_cottage = $_REQUEST.type == 'house' && !house.is_condo && !house.hasblocks

        data.record.item = house
        
        fill (view, data).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'living_room_new_form',

                record: data.record,

                fields : [                

                    {name: 'roomnumber', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'square', type: 'float', options: {min: 0}},
                    {name: 'f_20130', type: 'int', options: {min: 0}},
                    {name: 'f_20056', type: 'list', options: {
                        items: data.vc_nsi_261.items
                    }},                                                                           
                    {name: 'uuid_premise', type: 'list', options: {
                        items: (data.vc_premises || {}).items
                    }, off: !house.is_condo},
                    {name: 'uuid_block', type: 'list', options: {
                        items: (data.vc_blocks || {}).items
                    }, off: house.is_condo},

                ].filter (not_off),
                
                onRefresh: function (e) {

                    var form = w2ui [e.target]
                    
                    var record = form.record
                    
                    var enums = form.fields.filter (function (r) {return r.type == 'enum'})
                                        
                    if (enums.length) $.each (enums, function () {
                        var k = this.name
                        var v = record [k]
                        if (!Array.isArray (v) || !v.length || typeof v [0] === 'object') return
                        record [k] = this.options.items.filter (function (i) {return v.includes (i.id)})
                    })
                                    
                    if (is_single_cottage) $('#uuid_block').prop ({disabled: 1})            
                    
                },
                
                focus: is_single_cottage ? 2 : 1,

            }).refresh ()            

       })

    }

})