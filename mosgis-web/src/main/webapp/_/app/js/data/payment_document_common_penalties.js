define ([], function () {

    $_DO.patch_payment_document_common_penalties = function (e) {

        var grid = this

        var col = grid.columns [e.column]

        var v = normalizeValue (e.value_new, col.editable.type)

        var data = {}; 
        
        data [col.field] = v == null ? null : String (v)
        
        grid.lock ()
/*
        query ({type: 'charge_info', id: e.recid, action: 'update'}, {data: data}, function (d) {
            grid.unlock ()
            delete grid.get (e.recid) ['pp_sum']
            grid.set (e.recid, d.item)
            grid.refresh ()
        })
*/
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
            
            $.each (data.vc_nsi_329.items, function () {
            
                var nsi_329 = this

                $.each (ba, function () {                
                
                    lines.push ({
                        recid: nsi_329.id + '_' + ba.id,
                        label: nsi_329.label,
                        acct: this.label,
                        org_label: this.org_label,
                        id_type: 1,
                    })                                
                
                })            
                
            })
            
/*            
            $.each (dia2w2uiRecords (d.root), function () {
                
                if (!this.uuid_m_m_service) return

                lines.push (this)
            
            })
*/        
            data.lines = lines

            done (data)

        }) 

    }

})