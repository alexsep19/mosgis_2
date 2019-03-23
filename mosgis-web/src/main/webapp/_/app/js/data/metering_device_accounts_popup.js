define ([], function () {

    $_DO.update_metering_device_accounts_popup = function (e) {

        query (

            {type: 'metering_devices', id: $_REQUEST.id, action: 'set_accounts'},

            {data: {uuid_account: w2ui ['account_items_grid'].getSelection ()}},

            reload_page

        )

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        var v = {fiashouseguid: null, uuid_premise: null}
        
        for (k in v) v [k] = it [k]        
                
        query ({type: 'account_items', id: null}, {data: v, offset: 0, limit: 10000}, function (d) {
        
            var ids = {}
            data.account_items = []

            $.each (d.root, function () {
            
                if (!it.uuid_premise && this.uuid_premise) return
                
                var id = this ['acc.uuid']
                if (id in ids) return
                
                data.account_items.push ({
                    recid: id,
                    no:    this ['acc.accountnumber'],
                    label: this ['ind.label'] || this ['org.label']
                })
                
                ids [id] = 1
                
            })

            data.record = {}

            done (data)
        
        })        

    }

})