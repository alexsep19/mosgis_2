define ([], function () {

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        done(clone ($('body').data ('data')))

    }

})