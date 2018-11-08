define ([], function () {
    
    $_DO.create_charter_object_working_lists = function (e) {
            
        use.block ('charter_object_working_list_new')
    
    }    

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})