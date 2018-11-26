define ([], function () {

    $_DO.create_charter_payments = function (e) {            
    
        use.block ('charter_payment_new')   
        
    }
    
    function is_editable (it) {
    
        if ((it.uuid_org != $_USER.uuid_org) && !$_USER.role.admin) return 0;
        
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
        
        query ({type: "charter_objects", id: undefined}, {limit: 100000, offset: 0, search: [
            {field: "uuid_charter", operator: "is", value: it.uuid},            
        ]}, function (d) {
        
            var a = []
            
            $.each (d.root, function () {
                if (this.id_ctr_status_gis == 40) a.push ({id: this.id, text: this ['fias.label']})
            })
            
            data.charter_objects = a
            
            $('body').data ('data', data)
            
            if (a.length > 0) it._can.create_payment = 1

            done (data)

        })                      
        

    }

})