define ([], function () {   

    $_DO.print_tables = function (e) {
    
        this.owner.saveAsXLS ('Таблицы БД mosgis')
    
    }

    return function (done) {

        query ({type: 'tables'}, {}, function (data) {        

            $('body').data ('data', data)

            done (data)

        })

    }

})