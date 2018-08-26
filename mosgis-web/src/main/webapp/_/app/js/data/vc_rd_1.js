define ([], function () {

    return function (done) {
        
        query ({type: 'vc_rd_1', part: 'vocs'}, {}, function (data) {
            
            data.vc_rd_1240.unshift ({id: 0, label: '[пусто]'})
            data.vc_rd_1540.unshift ({id: 0, label: '[пусто]'})
           
            add_vocabularies (data, {
                vc_rd_1240: 1,
                vc_rd_1540: 1,
            })

            done (data)    
            
        })        
    
    }
    
})