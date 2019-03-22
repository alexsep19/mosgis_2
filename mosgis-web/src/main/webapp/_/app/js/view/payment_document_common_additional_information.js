define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        var $panel = $(w2ui ['passport_layout'].el ('main'))
                
        fill (view, data.item, $panel)
        
        if ($_SESSION.delete ('edit_payment_document_common')) $('textarea').removeAttr ('readonly')
    
    }

})