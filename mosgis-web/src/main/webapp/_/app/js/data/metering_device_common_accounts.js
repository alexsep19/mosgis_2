define ([], function () {

    $_DO.create_metering_device_common_accounts = function (e) {

        use.block ('metering_device_accounts_popup')

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))

        done (data)

    }

})