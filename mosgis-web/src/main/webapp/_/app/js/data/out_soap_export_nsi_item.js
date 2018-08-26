define ([], function () {
    
    $_DO.open_err_popup_out_soap_export_nsi_item = function (e) {
    
        var col = this.columns [e.column]
        
        if (col.field != 'cnt_failed') return 

        var d = this.get (e.recid)
        
        if (!(d.cnt_failed > 0)) return
        
        sessionStorage.setItem ('dt', d.id.substr (0, 10))

        use.block ('out_soap_export_nsi_item_err_popup')
    
    }

    return function (done) {

        query ({type: 'out_soap_export_nsi_item', part: 'stats'}, {data: w2ui ['out_soap_filter_form'].values ()}, function (data) {
            
            data.records = dia2w2uiRecords (data.records || [])
            
            $.each (data.records, function () {
                
                this.cnt_ok = this.cnt - this.cnt_failed
                
                if (!this.cnt_failed) delete this.cnt_failed
            
            })
                                
            done (data)
        
        })                   
        
    }
    
})