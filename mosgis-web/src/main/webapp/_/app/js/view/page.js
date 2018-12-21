
define ([], function () {
    
    return function (data, view) {           
        
        fill (view, data, $('body') )
        
        use.block ($_REQUEST.type || 'main')        
    
    }

});