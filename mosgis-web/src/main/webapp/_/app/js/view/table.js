define ([], function () {

    return function (data, view) {
        
        var it = data.item
                
        $('title').text ('Таблица ' + it.recid + ': ' + it.label)
        
        $('#body').attr ({valign: 'top'})

        fill (view, data, $('#body'))
        

    }

})