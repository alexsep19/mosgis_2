define ([], function () {
/*
    $_DO.update_acknowledgment_common_items = function (sum) {
        
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

    $_DO.patch_acknowledgment_common_items = function (e) {

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
        
        
        
        
        switch (col.field) {
            case 'cons_i_vol': 
            case 'cons_o_vol':
            case 'rate':
                data.accountingperiodtotal = null
                data.totalpayable = null
        }

        if (data.accountingperiodtotal == null) {
            data.accountingperiodtotal = parseFloat (data.rate) 
                * (parseFloat (data.cons_i_vol || '0') + parseFloat (data.cons_o_vol || '0'))
        }        
        
        
        
        switch (col.field) {
            case 'moneyrecalculation': 
            case 'moneydiscount': 
                data.totalpayable = null
        }

        if (data.totalpayable == null) {
            data.totalpayable = parseFloat (data.accountingperiodtotal) 
                + parseFloat (data.moneyrecalculation || '0') 
                - parseFloat (data.moneydiscount      || '0')
        }
        
        
        
        switch (col.field) {
            case 'cons_i_vol': 
            case 'rate':
            case 'ratio':
                data.amountofexcessfees = null
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
*/
    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')                
        
        var lines = [{recid: 'total', label: 'Всего по документу', id_type: -1}]
        
        function addLines (a) {
        
            var last_type = ''

            $.each (dia2w2uiRecords (a), function () {

                if (last_type != this.label_type) lines.push ({recid: String (Math.random ()).replace ('.', ''), label: last_type = this.label_type})

                lines.push (this)

            })
            
        }
        
        addLines (data.charges)
        
        var penalties = data.penalties 
        
        if (penalties && penalties.length) {
            
            lines.push ({recid: 'total', label: 'Неустойки и судебные расходы', id_type: -1})
            
            $.each (penalties, function () {
            
                this.label = this.cause
            
                this.id_type = this.code_vc_nsi_329
                this.label_type = data.vc_nsi_329 [this.code_vc_nsi_329]
                
            })
            
            addLines (penalties)

        }

        data.lines = lines

        done (data)        

    }

})