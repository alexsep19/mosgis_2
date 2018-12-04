define ([], function () {

    return function (done) {        
    
    	var data = clone ($('body').data ('data'))
        
    	data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})