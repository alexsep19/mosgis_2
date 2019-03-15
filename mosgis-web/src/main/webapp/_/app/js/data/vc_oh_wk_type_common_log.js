define ([], function () {

    return function (done) {

        w2ui ['passport_layout'].unlock('main')

        done ($('body').data ('data'))

    }

})