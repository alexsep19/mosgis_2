define ([], function () {

    return function (done) { 

        query ({type: 'out_soap_export_nsi_item', part: 'rp'}, {}, function (data) {
            
            data.xml = prettifyXml (data.xml.replace (/> +</g, '><'))

            done (data)

        })

    }

})