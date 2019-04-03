define ([], function () {   

    return function (done) {
    
        var data = {}

        var idx = clone ($('body').data ('vc_nsi_list'))
        
        var l = []
        
        for (var i in idx) l.push ({recid: i, label: idx [i]})

        done ({vc_nsi_list: l})
            
    }

})