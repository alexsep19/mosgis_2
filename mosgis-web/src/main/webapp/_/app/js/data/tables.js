define ([], function () {   
/*
    $_DO.open_tables = function (e) {
        
        var id = e.target
        
        var type = 
            parseInt (id) > 0 ? 'voc_nsi' : 
            id

        if (!type || /[A-Z]$/.test (type)) return
                
        w2utils.lock ($(w2ui ['vocs_layout'].el ('main')), {spinner: true})

        use.block (type)

    }
*/
    return function (done) {

        query ({type: 'tables'}, {}, function (data) {        

            $('body').data ('data', data)

            done (data)

        })

    }

})