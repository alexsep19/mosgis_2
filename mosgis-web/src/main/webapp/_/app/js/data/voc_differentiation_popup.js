define ([], function () {

    return function (done) {        

        done (clone($('body').data('data')))

    }

})