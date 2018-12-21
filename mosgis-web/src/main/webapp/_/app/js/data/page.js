define ([], function () {

    return function (done) {        
                
        done ({
            user_label: $_USER.label,
            org_label: $_USER.label_org,
        })
        
    }
    
})