define ([], function () {

    return function (data, view) {
    
        var house = data.item
        
        var entrances = data.entrances.items
        entrances.unshift ({id: "", text: "Отдельный вход"})

        var min_year = house.usedyear || 1600

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'premise_residental_new_form',

                record: data.record,
/*                                
                tabs: [
                    {id: 1, caption: 'Основные'},
                    {id: 2, caption: 'Дополнительно'},
                ],
*/                    
                fields : [                
                
                    {name: 'uuid_entrance', type: 'list', options: {
                        items: entrances
                    }},
                    {name: 'premisesnum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'code_vc_nsi_30', type: 'list', options: {
                        items: data.vc_nsi_30.items.map (function (i) {return {
                            id: i.id,
                            text: i.text.replace ('Квартира коммунального заселения', 'Коммунальн. квартира')
                        }})
                    }},
                    {name: 'totalarea', type: 'float', options: {min: 0}},
                    {name: 'grossarea', type: 'float', options: {min: 0}},
                    {name: 'f_20002', type: 'list', options: {
                        items: data.vc_nsi_14.items
                    }},
                    {name: 'f_20125', type: 'int', options: {min: 0}},
                    {name: 'f_20061', type: 'list', options: {
                        items: data.vc_nsi_259.items
                    }},
                    {name: 'f_20059', type: 'list', options: {
                        items: data.vc_nsi_258.items
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