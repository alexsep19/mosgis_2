define ([], function () {

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        done ($('body').data ('data'))

    }

})