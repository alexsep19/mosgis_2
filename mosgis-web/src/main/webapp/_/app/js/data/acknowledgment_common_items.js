define ([], function () {

    $_DO.patch_acknowledgment_common_items = function (e) {

        var grid = this
        var cols = grid.columns
        var col  = cols [e.column]
        var editable = col.editable
        var v = normalizeValue (e.value_new, editable.type)

        var data = {uuid_ack: $_REQUEST.id};

        data [col.field] = v == null ? null : String (v)
        
        var row = grid.get (e.recid)
        
        if (row.code_vc_nsi_329) {
            data.uuid_penalty = row.uuid
        }
        else {
            data.uuid_charge = row.uuid
        }        

        grid.lock ()

        query ({type: 'acknowledgments', action: 'patch'}, {data: data}, function (d) {
        
            var form = w2ui ['acknowledgment_common_form']
            
            form.record.amount = d.item.amount            
            form.unlock ()
            form.refresh ()
        
            grid.unlock ()
            
            data = {}
            
            var line = d.line
            
            data.amount_ack = parseFloat (line.amount_ack)
            data.amount_nack = parseFloat (line.totalpayable) - data.amount_ack

            grid.set (e.recid, data)
            grid.refresh ()
            
        })

    }

    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')                
        
        var lines = [{recid: 'total', label: 'Всего по документу', id_type: -1}]
        
        var idx = {}
        
        function addLines (a) {
        
            var last_type = ''

            $.each (dia2w2uiRecords (a), function () {

                if (last_type != this.label_type) lines.push ({recid: String (Math.random ()).replace ('.', ''), label: last_type = this.label_type})

                lines.push (this)
                
                idx [this.uuid] = this

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
        
        $.each (data.items, function () {        
            idx [this.uuid_charge || this.uuid_penalty].amount = this.amount
        })

        data.lines = lines

        done (data)        

    }

})