define ([], function () {

    return function (done) { 

        query ({type: 'ws_msgs', part: 'rp'}, {}, function (data) {
            
            data.xml = prettifyXml (data.xml.replace (/> +</g, '><'))

            done (data)

        })

    }

})