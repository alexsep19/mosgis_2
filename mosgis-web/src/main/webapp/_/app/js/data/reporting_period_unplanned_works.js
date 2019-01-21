define ([], function () {

    $_DO.create_reporting_period_unplanned_works = function (e) {
        $_SESSION.set ('record', {count: 1})
        use.block ('reporting_period_unplanned_works_popup')
    }
    
    $_DO.edit_reporting_period_unplanned_works = function (e) {
        $_SESSION.set ('record', this.get (e.recid))
        use.block ('reporting_period_unplanned_works_popup')
    }

    $_DO.delete_reporting_period_unplanned_works = function (e) {
    
        if (!e.force) return
        
        var grid = w2ui [e.target]
        
        grid.lock ()
        
        query ({type: 'unplanned_works', id: grid.getSelection () [0], action: 'delete'}, {}, function () {
        
            use.block ('reporting_period_unplanned_works')
            
        })
        
    }

    return function (done) {

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))

        done (data)

    }

})