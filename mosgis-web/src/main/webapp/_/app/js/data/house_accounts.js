define ([], function () {
    
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))
        
        var it = data.item
                
        query ({type: 'account_items', id: null}, {limit: 1000000, offset: 0, data: {fiashouseguid: it.fiashouseguid}}, function (d) {
        
            var idx = {}
            
            $.each (d.root, function () {
                var uuid = this.uuid_account
                if (!idx [uuid]) idx [uuid] = []
                idx [uuid].push (this)
            })
        
            var lines = []
            
            for (var uuid in idx) {
                var a = idx [uuid]
                var i = a [0]
                i.recid = uuid
                i.accountnumber = i ['acc.accountnumber'] 
                i.id_type = i ['acc.id_type']
                i.id_ctr_status = i ['acc.id_ctr_status']
                i.serviceid = i ['acc.serviceid']
                i.owner = i ['org_owner.label']
                i.customer = i ['org.label'] || i ['ind.label']
                i.premises = a.map (function (r) {return r ['prem.label']}).sort ().join (', ')
                lines.push (i)
            }
                
            data.lines = lines.sort (function (a, b) {return a.accountnumber < b.accountnumber ? -1 : 1})

            done (data);            

        })

    }

})