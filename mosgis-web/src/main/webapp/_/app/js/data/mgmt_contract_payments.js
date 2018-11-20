define ([], function () {

    $_DO.create_mgmt_contract_payments = function (e) {            
    
        use.block ('mgmt_contract_payment_new')   
        
    }
/*
    $_DO.add_mgmt_contract_payments = function (e) {
    
        use.block ('mgmt_contract_payment_popup')
        
    }   
*/
    
    function is_editable (it) {
    
        if (it.id_ctr_status != 40) return 0;
        
        switch (it.id_ctr_status_gis) {
            case 20:
            case 30:
            case 40:
                return 1
            default:                    
                return 0
        }
            
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))
        
        var it = data.item

        if (!is_editable (it)) return done (data)
        
        query ({type: "contract_objects", id: undefined}, {limit: 100000, offset: 0, search: [
            {field: "uuid_contract", operator: "is", value: it.uuid},            
        ]}, function (d) {
        
            var a = []
            
            $.each (d.root, function () {
                if (this.id_ctr_status_gis == 40) a.push ({id: this.id, text: this ['fias.label']})
            })
            
            data.contract_objects = a
            
            $('body').data ('data', data)
            
            if (a.length > 0) it._can.create_payment = 1

            done (data)

        })                      
        

    }

})