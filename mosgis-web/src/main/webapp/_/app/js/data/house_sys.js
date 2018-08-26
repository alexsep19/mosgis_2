define ([], function () {

    $_DO.patch_house_sys = function (e) {    
    
        var grid = this
    
        var col = grid.columns [e.column]
        
        var editable = col.editable (grid.get (e.recid))

        var data = {
            k: 'f_' + e.recid,
            v: normalizeValue (e.value_new, editable.type)
        }
        
        if (data.v != null) data.v = String (data.v)

        var house = $('body').data ('data')
        
        var depends  = house.depends

        var children = []; for (var i in depends) if (depends [i] == e.recid) children.push ('f_' + i)
        
        var fns = []
            
        if (children.length && data.v != 1) {

            fns = children.filter (function (fn) {return house.item [fn]})
            
            if (fns.length && !confirm ('Все ранее заполненные показатели по данной внутридомовой системе будут очищены. Продолжить?')) return

        }

        grid.lock ()

        var tia = {type: 'houses', action: 'patch'}

        query (tia, {data: data}, function () {

            house.item [data.k] = data.v == "0" ? 0 : data.v

            $.each (fns, function () {                
                delete house.item [this]
                query (tia, {data: {k: this, v: null}}, $.noop)
            })

            $('body').data ('data', house)

            grid.unlock ()                

            var tabs = w2ui ['house_systems_layout'].get('main').tabs

            if (data.v == 1) {
                tabs.show (e.recid)
            }
            else {
                tabs.hide (e.recid)
            }            

            $_F5 ()

        })

    }

    return function (done) {
    
        var house = $('body').data ('data')
                
        house.doc_fields = {}

        query ({type: 'houses', part: 'passport_fields_sys'}, {}, function (data) {
        
            var fields = data.vc_pass_fields
            
            var vocs = {}
            
            $.each (fields, function () {
            
                this.recid = this.id
                
                this.name = 'f_' + this.id

                this.value = house.item [this.name]
                                                
                if (this.is_mandatory) this.w2ui = {class: 'status_warning'}
                                
                if (this.voc) {
                
                    var name = 'vc_nsi_' + this.voc
                    
                    if (data [name]) vocs [name] = 1
                                    
                }
                
                if (this.id_type == 3) {
                    house.doc_fields [this.id] = 1
                    house.doc_fields [this.id_dt] = 1
                    house.doc_fields [this.id_no] = 1
                }
                    
            })
            
            add_vocabularies (data, vocs)
            
            for (i in vocs) house [i] = data [i]
            
            house.vc_pass_fields = fields
            
            $('body').data ('data', house)

            done (house);

        }) 

        w2ui ['topmost_layout'].unlock ('main')
                

    }

})