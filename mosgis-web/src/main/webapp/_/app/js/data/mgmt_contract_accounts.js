define ([], function () {

    $_DO.create_mgmt_contract_accounts = function (e) {           
    
        use.block ('account_new')
        
    }  
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})