define ([], function () {

    return function (data, view) {

        var house = data.item
        
        var min_year = house.usedyear || 1600

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'block_new_form',

                record: data.record,
                                /*
                tabs: [
                    {id: 1, caption: 'Основные'},
                    {id: 2, caption: 'Дополнительно'},
                ],
                */    
                fields : [                
                
                    {name: 'blocknum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'code_vc_nsi_30', type: 'list', options: {
                        items: data.vc_nsi_30.items
                    }, _no_nrs: 1},
                    {name: 'totalarea', type: 'float', options: {min: 0}},
                    {name: 'grossarea', type: 'float', options: {min: 0}, _no_nrs: 1},
                    {name: 'f_20002', type: 'list', options: {
                        items: data.vc_nsi_14.items
                    }, _no_nrs: 1},
                    {name: 'f_20125', type: 'int', options: {min: 0}, _no_nrs: 1},
                    {name: 'f_20061', type: 'list', options: {
                        items: data.vc_nsi_259.items
                    }, _no_nrs: 1},
                    {name: 'f_20059', type: 'list', options: {
                        items: data.vc_nsi_258.items
                    }, _no_nrs: 1},                    

                    {name: 'f_20054', type: 'list', options: {
                        items: data.vc_nsi_254.items
                    }},                    
                    {name: 'f_20053', type: 'list', options: {
                        items: data.vc_nsi_253.items
                    }},                    
                    {name: 'f_20056', type: 'list', options: {
                        items: data.vc_nsi_261.items
                    }},                    
                    
                    {name: 'is_nrs', type: 'list', options: {
                        items: data.vc_is_nrs.items
                    }},                    
                
                ],

                onChange: function (e) {

                    var form = this

                    if (e.target == "is_nrs") {

                        e.done (function () {

                            var off = e.value_new.id == 1
                            
                            $.each (form.fields, function () {                            
                                if (!this._no_nrs) return
                                this.$el.prop ('disabled', off)
                                if (off) delete form.record [this.name]
                            })
                            
                            if (!off && !form.record.code_vc_nsi_30) form.record.code_vc_nsi_30 = data.vc_nsi_30.items [2]

                            form.refresh ()
                            
                        })

                    }               

                },
                
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
                
                },
                
                focus: 1,

            })

       })

    }

})