define ([], function () {
    
    return function (done) { 

        query ({type: 'ws_msgs', part: 'rq'}, {}, function (data) {
            
            data.xml = prettifyXml (data.xml)

            done (data)

        })

    }

})