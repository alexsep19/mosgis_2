define ([], function () {

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))
    
        $.each (data.plans, function () {this.recid = this.uuid})

        done (data)
                        
    }

})