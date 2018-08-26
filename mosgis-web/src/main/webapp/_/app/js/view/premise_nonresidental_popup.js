define ([], function () {

    return function (data, view) {
    
        var house = data.item
        
        var min_year = house.usedyear || 1600

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'premise_nonresidental_new_form',

                record: data.record,

                fields : [                
                
                    {name: 'premisesnum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},

                    {name: 'iscommonproperty', type: 'list', options: {
                        items: [{id: "0", text: "Нет"},{id: "1", text: "Да"}]
                    }},

                    {name: 'totalarea', type: 'float', options: {min: 0}},

                    {name: 'f_20003', type: 'list', options: {
                        items: data.vc_nsi_17.items
                    }},


                    {name: 'f_20054', type: 'list', options: {
                        items: data.vc_nsi_254.items
                    }},                    
                    {name: 'f_20053', type: 'list', options: {
                        items: data.vc_nsi_253.items
                    }},                    
                    {name: 'f_20056', type: 'list', options: {
                        items: data.vc_nsi_261.items
                    }},                    
                
                ],
                
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
                
                }
                
//                focus: 1,

            })

       })

    }

})