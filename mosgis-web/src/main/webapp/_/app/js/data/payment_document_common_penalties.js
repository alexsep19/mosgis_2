define ([], function () {

    $_DO.update_payment_document_common_penalties = function (sum) {
    
        var k = 'totalbypenaltiesandcourtcosts'
        
        var data = {}; data [k] = sum

        query ({type: 'payment_documents', action: 'update'}, {data: data}, function (d) {
            var f = w2ui ['payment_document_common_form']            
            f.record [k] = d.item [k]
            f.refresh ()
        })

    }

    $_DO.patch_payment_document_common_penalties = function (e) {

        var grid = this

        var col = grid.columns [e.column]

        var v = normalizeValue (e.value_new, col.editable.type)

        var data = {}; 

        data [col.field] = v == null ? null : String (v)

        var nsi329_ba = e.recid.split ('_')

        data.code_vc_nsi_329 = nsi329_ba [0]
        data.uuid_bnk_acct   = nsi329_ba [1]

        grid.lock ()

        query ({type: 'payment_documents', action: 'patch_penalties'}, {data: data}, function (d) {
            grid.unlock ()
            grid.refresh ()            
        })

    }

    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')                

        query ({type: 'payment_documents', part: 'charge_info'}, {}, function (d) {

            var ba = {}

            $.each (d.root, function () {
            
                var id = this.uuid_bnk_acct

                ba [id] = {
                    id: id,
                    label: this ['ba.label'],
                    org_label: this ['org_bank_acct.label'],
                }

            })
            
            ba = Object.values (ba).sort (function (a, b) {return a.label < b.label ? -1 : 1})        
        
            var lines = [{recid: 'total', label: 'Итого'}]
            
            var idx = {}
            
            $.each (data.vc_nsi_329.items, function () {
            
                var nsi_329 = this

                $.each (ba, function () {                
                
                    var id = nsi_329.id + '_' + this.id
                
                    lines.push (idx [id] = {
                        recid: id,
                        label: nsi_329.label,
                        acct: this.label,
                        org_label: this.org_label,
                        id_type: 1,
                    })                                
                
                })            
                
            })
            
            query ({type: 'payment_documents', part: 'penalties'}, {}, function (dd) {

                $.each (dd.root, function () {                

                    var r = idx [this.code_vc_nsi_329 + '_' + this.uuid_bnk_acct]

                    r.totalpayable = this.totalpayable
                    r.cause = this.cause

                })

                data.lines = lines

                done (data)
            
            });
            
            
/*            
            $.each (dia2w2uiRecords (d.root), function () {
                
                if (!this.uuid_m_m_service) return

                lines.push (this)
            
            })
*/        

        }) 

    }

})