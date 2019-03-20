define ([], function () {

    $_DO.import_check_plans_import = function (e) {

        var form = w2ui ['check_plans_import']

        var v = form.values ()
        
        if (v.year_from && (v.year_from < 1992 || v.year_from > 2030)) die ('year', 'Пожалуйста, укажите корректное значение года c (1992 - 2030)')
        if (v.year_to && (v.year_to < 1992 || v.year_to > 2030)) die ('year', 'Пожалуйста, укажите корректное значение года по (1992 - 2030)')

        var grid = w2ui ['check_plans_grid']

        query ({type: 'check_plans', action: 'import'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

    	done ()

    }

})