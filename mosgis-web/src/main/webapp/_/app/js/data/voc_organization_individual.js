define ([], function () {

    return function (done) {

        query ({type: 'voc_organizations'}, {}, function (data) {

            $('body').data ('data', data)

            get_nsi ([20], done)

        })

    }

})