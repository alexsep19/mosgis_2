define ([], function () {

    $_DO.create_account_common_items = function (e) {

        use.block ('account_item_popup')

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))

        done (data)
                
    }

})