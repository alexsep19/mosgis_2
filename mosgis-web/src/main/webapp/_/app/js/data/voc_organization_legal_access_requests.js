define ([], function () {

    return function (done) {        

        var layout = w2ui ['topmost_layout']

        if (layout) layout.unlock ('main')
        
        query ({type: 'access_requests', id: undefined}, {data: {orgrootentityguid: $_REQUEST.id}}, function (d) {

            var data = clone ($('body').data ('data'))

            $.each (d.tb_acc_req, function () {this.id = this.accessrequestguid})

            data.records = dia2w2uiRecords (d.tb_acc_req)            
darn (data)
            done (data)

        })
                
    }

})