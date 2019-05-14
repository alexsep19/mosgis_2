define ([], function () {

    $_DO.print_table = function (e) {
    
        var data = $('body').data ('data')
    
        var it = data.item
        
        var $t = $('table.ttt').clone ()
        
        $t.prepend ('<tr><th colspan=8 align=left>&nbsp;Таблица ' + it.recid + ': ' + it.label)
                
        $t.saveAsXLS ('Таблица ' + it.recid + ' (' + it.label + ')')
    
    }

    return function (done) {

        query ({type: 'tables'}, {}, function (data) {        
        
            $('body').data ('data', data)
    
            done (data)

        })
        
    }

})