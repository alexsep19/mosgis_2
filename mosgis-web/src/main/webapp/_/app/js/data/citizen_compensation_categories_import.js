define ([], function () {

    $_DO.import_citizen_compensation_categories_import = function (e) {

        var form = w2ui ['citizen_compensation_categories_import']

        var v = form.values ()
        
        if (!v.fromdate) die ('fromdate', 'Пожалуйста, укажите дату начала предоставления')


        w2popup.close ()

        w2ui ['citizen_compensation_categories_grid'].lock()

        $_SESSION.set('citizen_compensation_categories_importing', 1)


        query ({type: 'citizen_compensation_categories', action: 'import'}, {data: v}, $_DO.check_citizen_compensation_categories_import)

    }

    $_DO.check_citizen_compensation_categories_import = function () {

        var grid = w2ui ['citizen_compensation_categories_grid']

        query ({type: 'citizen_compensation_categories', id: null, part: 'log'}, {}, function (d) {

            var is_importing = $_SESSION.get ('citizen_compensation_categories_importing')

            if (!d.log.uuid) return is_importing ? null : $_DO.import_citizen_compensation_categories_import ()
        
            if (d.log.is_over) {            
                $_SESSION.delete ('citizen_compensation_categories_importing')
                if (is_importing && grid) use.block ('citizen_compensation_categories')
                return
            }
                        
            setTimeout (function () {w2ui ['citizen_compensation_categories_grid'].lock ('Запрос в ГИС ЖКХ...', 1)}, 10)

            setTimeout ($_DO.check_citizen_compensation_categories_import, 5000)

        })
    }

    return function (done) {

    	done ()

    }

})