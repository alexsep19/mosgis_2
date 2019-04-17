define ([], function () {

    $_DO.create_payment_acknowledgments = function (e) {
    
        use.block ('payment_acknowledgment_new')

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        done ($('body').data ('data'))
                
    }

})