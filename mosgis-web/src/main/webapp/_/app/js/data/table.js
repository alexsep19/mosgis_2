define ([], function () {

    $_DO.print_table = function (e) {
    
        var data = $('body').data ('data')
    
        var it = data.item
                
        $('table.ttt').saveAsXLS ('Таблица ' + it.recid + ' (' + it.label + ')')
    
    }

    return function (done) {

        query ({type: 'tables'}, {}, function (data) {        
        
            $('body').data ('data', data)
    
            done (data)

        })
        
    }

})