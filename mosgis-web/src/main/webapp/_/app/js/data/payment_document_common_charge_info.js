define ([], function () {

    $_DO.patch_payment_document_common_charge_info = function (e) {

        var grid = this
        var col = grid.columns [e.column]

        var editable = col.editable
        if (typeof editable === "function") editable = editable (grid.get (e.recid))

        var v = normalizeValue (e.value_new, editable.type)

        var data = {}; data [col.field] = v == null ? null : String (v)

        grid.lock ()

        query ({type: 'charge_info', id: e.recid, action: 'update'}, {data: data}, function () {
            grid.unlock ()
            grid.refresh ()
//            $_F5 ()
        })

    }

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