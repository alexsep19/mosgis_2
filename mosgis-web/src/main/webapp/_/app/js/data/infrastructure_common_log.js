define ([], function () {

    return function (done) {        

        var layout = w2ui ['topmost_layout']

        if (layout) layout.unlock ('main')

        done ($('body').data ('data'))
                
    }

})