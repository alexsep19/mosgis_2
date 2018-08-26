define ([], function () {

    $_DO.patch_premise_nonresidental_passport = function (e) {    
    
        var grid = this
    
        var col = grid.columns [e.column]
        
        var editable = col.editable (grid.get (e.recid))

        var d = {
            k: 'f_' + e.recid,
            v: normalizeValue (e.value_new, editable.type)
        }
        
        if (d.v != null) d.v = String (d.v)

        grid.lock ()
        
        var tia = {type: 'premises_nonresidental', action: 'update'}
        var dd = {}; dd [d.k] = d.v

        query (tia, {data: dd}, function () {

            var data = $('body').data ('data')
            data.item [d.k] = d.v            
            $('body').data ('data', data)                                                
            grid.unlock ()                
            $_F5 ()
                
        })
    
    }

    return function (done) {
    
        var data = $('body').data ('data')
                
        data.doc_fields = {}

        query ({type: 'premises_nonresidental', part: 'passport_fields'}, {}, function (d) {
        
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