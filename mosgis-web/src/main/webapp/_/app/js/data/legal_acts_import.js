define ([], function () {

    $_DO.import_legal_acts_import = function (e) {

        var form = w2ui ['legal_acts_import']

        var v = form.values ()

        if (!v.level_) die('level_', 'Пожалуйста, укажите уровень')

        if (!v.acceptstartdate) die ('acceptstartdate', 'Пожалуйста, укажите начало периода принятия органом государственной власти')

        var grid = w2ui ['legal_acts_grid']

        query ({type: 'legal_acts', action: 'import'}, {data: v}, function (data) {
        
            w2popup.close ()

            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

    	done (clone($('body').data('data')))

    }

})