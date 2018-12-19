define ([], function () {

    $_DO.create_public_property_contract_agreement_payments = function (e) {

        use.block ('agreement_payment_new')

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        var it = data.item

//        if (!is_editable (it)) return done (data)
/*        
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
*/        

        done (data)

    }

})