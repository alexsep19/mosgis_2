define ([], function () {

    return function (data, view) {
        
        $('title').text ($_REQUEST.id)
    
        fill (view, data, $('body'))

    }    
    
})