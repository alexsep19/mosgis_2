define ([], function () {

    $_DO.update_payment_document_common_charge_info = function (sum) {
        
        var data = {
            totalpayablebypd: sum,
            totalpayablebypdwith_da: null,
        }
        
        var f = w2ui ['payment_document_common_form']
        var r = f.record
                
        query ({type: 'payment_documents', action: 'update'}, {data: data}, function (d) {
            f.record.totalpayablebypd = d.item.totalpayablebypd
            f.record.totalpayablebypdwith_da = d.item.totalpayablebypdwith_da            
            f.refresh ()
        })

    }

    $_DO.patch_payment_document_common_charge_info = function (e) {

        var grid = this
        var cols = grid.columns
        var col  = cols [e.column]

        var editable = col.editable
        if (typeof editable === "function") editable = editable (grid.get (e.recid))

        var v = normalizeValue (e.value_new, editable.type)

        var data = {}; data [col.field] = v == null ? null : String (v)
        
        var row = grid.get (e.recid)
        
        var flds = []
                
        for (var i = 0; i < cols.length; i ++) {
            var c = cols [i]
            if (!('editable' in c)) continue
            var editable = c.editable            
            if (typeof editable === "function") continue
            if (editable.type != 'float') continue
            flds.push (c.field)
        }

        $.each (flds, function () {if (this != col.field) data [this] = row [this]})

        if (data.accountingperiodtotal == null) {
            data.accountingperiodtotal = parseFloat (data.rate) 
                * (parseFloat (data.cons_i_vol || '0') + parseFloat (data.cons_o_vol || '0'))
        }        
        
        if (data.totalpayable == null) {
            data.totalpayable = parseFloat (data.accountingperiodtotal) 
                + parseFloat (data.moneyrecalculation || '0') 
                - parseFloat (data.moneydiscount      || '0')
        }

        if (data.ratio == null) {
            data.amountofexcessfees = null
        }
        else {        
            if (data.amountofexcessfees == null) {
                data.amountofexcessfees = parseFloat (data.rate)
                    *  parseFloat (data.cons_i_vol || '0')
                    * (parseFloat (data.ratio || '0') - 1.0)
            }
        }

        grid.lock ()

        query ({type: 'charge_info', id: e.recid, action: 'update'}, {data: data}, function (d) {
            grid.unlock ()
            var row = d.item
            data = {w2ui: {changes: {}}}
            $.each (flds, function () {data [this] = row [this] || null})
            var fld = col.field
            if (fld in data) data.w2ui.changes [fld] = data [fld]
            grid.set (e.recid, data)
            grid.refresh ()
        })

    }

    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')                

        query ({type: 'payment_documents', part: 'charge_info'}, {}, function (d) {
        
            var lines = [{recid: 'total', label: 'Всего по документу', id_type: -1}]
            
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