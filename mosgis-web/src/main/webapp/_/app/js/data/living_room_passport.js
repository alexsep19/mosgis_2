define ([], function () {

    $_DO.patch_living_room_passport = function (e) {    
    
        var grid = this
    
        var col = grid.columns [e.column]
        
        var editable = col.editable (grid.get (e.recid))

        var d = {
            k: 'f_' + e.recid,
            v: normalizeValue (e.value_new, editable.type)
        }
        
        if (d.v != null) d.v = String (d.v)

        grid.lock ()
        
        var tia = {type: 'living_rooms', action: 'update'}
        var dd = {}; dd [d.k] = d.v || null

        query (tia, {data: dd}, function () {

            var data = $('body').data ('data')
            data.item [d.k] = d.v            
            $('body').data ('data', data)            
                                    
            if (d.k == 'f_20131') { // непригодность

                var tabs = w2ui ['topmost_layout'].get ('main').tabs
                
                var id = "living_room_invalid"
                
                if (d.v == 0) {
                
                    query ({type: 'living_rooms', action: 'update'}, {data: {

                        f_20132: null, 
                        f_20133: null, 
                        f_20134: null, 

                    }}, function () {

                        if (data.file) {

                            query ({

                                type:   'house_docs', 
                                id:     data.file.uuid,
                                action: 'delete',

                            }, {}, reload_page)

                        }
                        else {
                            reload_page ()
                        }

                    })
                    
                }
                else {
                
                    grid.unlock ()
                    tabs.show (id)
                    $_F5 ()

                    if (confirm ('Перейти ко вводу документа о непригодности для проживания?')) tabs.click (id)
                        
                }

            }
            else {
            
                grid.unlock ()                
                $_F5 ()
                
            }

        })
    
    }

    return function (done) {
    
        var data = $('body').data ('data')
                
        data.doc_fields = {}

        query ({type: 'living_rooms', part: 'passport_fields'}, {}, function (d) {
                
            var fields = d.vc_pass_fields
            
            var vocs = {}
            
            $.each (fields, function () {
            
                this.recid = this.id
                
                this.name = 'f_' + this.id

                this.value = data.item [this.name]
                                                
                if (this.is_mandatory) this.w2ui = {class: 'status_warning'}
                                
                if (this.voc) {
                
                    var name = 'vc_nsi_' + this.voc
                    
                    if (d [name]) vocs [name] = 1
                                    
                }
                
                if (this.id_type == 3) {
                    data.doc_fields [this.id] = 1
                    data.doc_fields [this.id_dt] = 1
                    data.doc_fields [this.id_no] = 1
                }
                    
            })
            
            add_vocabularies (d, vocs)
            
            for (i in vocs) data [i] = d [i]
            
            data.vc_pass_fields = fields
            
            $('body').data ('data', data)

            done (data);

        }) 

        w2ui ['topmost_layout'].unlock ('main')               

    }

})