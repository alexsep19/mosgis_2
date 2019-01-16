define ([], function () {

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        var idx = {}
    
        $.each (data.plans, function () {
        
            idx [this.recid = this.uuid] = this
            
        })
        
        $.each (data.tb_reporting_periods, function () {
        
            var plan = idx [this.uuid_working_plan]
            
            if (plan) plan ['month_' + this.month] = this.uuid
            
        })        

        done (data)
                        
    }

})