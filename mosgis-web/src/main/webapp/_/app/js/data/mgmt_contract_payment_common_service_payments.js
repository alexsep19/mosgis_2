define ([], function () {

    $_DO.patch_mgmt_contract_payment_common_service_payments = function (e) {

        var grid = this
    
        var col = grid.columns [e.column]
                
        var data = {
            k: col.field,
            v: normalizeValue (e.value_new, col.editable.type)
        }
        
        if (data.k == 'servicepaymentsize') {
        
            if (data.v != null && !(data.v > 0)) {
                e.preventDefault ()
                return alert ('Размер платы должен быть положительным числом')
            }
            
        }        
        
        if (data.v != null) data.v = String (data.v)

        grid.lock ()
        
        var tia = {type: 'service_payments', action: 'update', id: e.recid}
        
        var d = {}; d [data.k] = data.v

        query (tia, {data: d}, function () {
        
            grid.unlock ()                    

            grid.refresh ()

        }, edit_failed (grid, e))

    }

    $_DO.delete_mgmt_contract_payment_common_service_payments = function (e) {
    
        if (!e.force) return
        
        var grid = w2ui [e.target]
        
        grid.lock ()
        
        query ({type: 'service_payments', id: grid.getSelection () [0], action: 'delete'}, {}, function () {
        
            use.block ('mgmt_contract_payment_common_service_payments')
            
        })
        
    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = $('body').data ('data')

        query ({type: 'service_payments', id: undefined}, {offset: 0, limit: 10000, data: {uuid_contract_payment: $_REQUEST.id}}, function (d) {
        
            data.records = dia2w2uiRecords (d.tb_svc_payments)
        
            done (clone (data))
        
        })
                
    }

})