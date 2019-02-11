define ([], function () {

    $_DO.create_supply_resource_contract_intervals = function (e) {           
        
        use.block ('interval_new')
        
    }  
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})