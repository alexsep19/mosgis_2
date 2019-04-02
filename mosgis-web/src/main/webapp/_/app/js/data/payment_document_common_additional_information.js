define ([], function () {

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        done (clone ($('body').data ('data')))
                
    }

})