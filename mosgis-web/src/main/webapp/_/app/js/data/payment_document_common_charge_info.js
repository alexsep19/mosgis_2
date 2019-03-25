define ([], function () {
/*
    $_DO.patch_payment_document_common_charge_info = function (e) {    
    
        var grid = this
    
        var col = grid.columns [e.column]
        
        var editable = col.editable (grid.get (e.recid))

        var data = {
            k: 'f_' + e.recid,
            v: normalizeValue (e.value_new, editable.type)
        }
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'houses', action: 'patch'}

        query (tia, {data: data}, function () {

            var house = $('body').data ('data')
            house.item [data.k] = data.v            

            $('body').data ('data', house)            
            
            if ($('body').data ('area_codes') [e.recid]) setTimeout ($_DO.check_sum_area_fields_of_a_house, 400)
            
            grid.unlock ()                
            $_F5 ()

        })
    
    }
*/
    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')                

        query ({type: 'payment_documents', part: 'charge_info'}, {}, function (d) {
        
            var lines = []            
            var last_type = ''
            
            $.each (dia2w2uiRecords (d.root), function () {
            
                if (last_type != this.label_type) lines.push ({recid: String (Math.random ()).replace ('.', ''), label: last_type = this.label_type})
                
                lines.push (this)
            
            })
        
            data.lines = lines

            done (data)

        }) 

    }

})