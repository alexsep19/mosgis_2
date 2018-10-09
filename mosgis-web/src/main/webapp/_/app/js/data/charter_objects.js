define ([], function () {
    
    $_DO.create_charter_objects = function (e) {
            
        use.block ('charter_object_new')
    
    }   

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})