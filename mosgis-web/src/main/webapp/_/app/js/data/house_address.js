define ([], function () {

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')

        var data = $('body').data ('data')
        
        var item = data.item

        for (i in item) if (i.indexOf ('.') >= 0) item [i.replace ('.', '_')] = item [i]

        done (data)

    }

})