define ([], function () {

    $_DO.patch_payment_document_common_comp_cost = function (e) {

        var grid = this

        var col = grid.columns [e.column]

        var v = normalizeValue (e.value_new, col.editable.type)

        var data = {}; 

        data [col.field] = v == null ? null : String (v)

        data.code_vc_nsi_331 = e.recid

        grid.lock ()

        query ({type: 'payment_documents', action: 'patch_comp_cost'}, {data: data}, function (d) {
            grid.unlock ()
            grid.refresh ()
        })

    }

    return function (done) {

    var layout = w2ui ['passport_layout']

    if (layout) layout.unlock ('main')

    var data = $('body').data ('data')                

        var lines = [{recid: 'total', label: 'Итого'}]
        
        var idx = {}
        
        $.each (data.vc_nsi_331.items, function () {
        
            var id = this.id

            lines.push (idx [id] = {
                recid: id,
                label: this.label,
                id_type: 1,
            })

        })

        query ({type: 'payment_documents', part: 'comp_cost'}, {}, function (dd) {

            $.each (dd.root, function () {                

                var r = idx [this.code_vc_nsi_331]

                r.cost = this.cost

            })

            data.lines = lines

            done (data)
        
        })

    }

})