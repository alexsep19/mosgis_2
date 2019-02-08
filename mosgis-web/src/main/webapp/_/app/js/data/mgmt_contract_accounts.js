define ([], function () {

    $_DO.create_mgmt_contract_accounts = function (e) {           
        
        use.block ('account_' + e.target + '_new')
        
    }  
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})