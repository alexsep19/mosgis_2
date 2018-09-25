define ([], function () {
    
    return function (done) { 

        query ({type: 'out_soap_export_nsi_item', part: 'rq'}, {}, function (data) {
            
            data.xml = prettifyXml (data.xml)

            done (data)

        })

    }

})