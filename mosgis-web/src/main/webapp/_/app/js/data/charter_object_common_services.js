define ([], function () {

    $_DO.create_charter_object_common_services = function (e) {

        use.block ('charter_object_common_service_' + e.target.split ('_') [1] + '_new')

    }   
    
    $_DO.edit_charter_object_common_services = function (e) {

        use.block ('charter_object_common_service_popup')

    }   

    $_DO.delete_charter_object_common_services = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'charter_object_services', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['passport_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})