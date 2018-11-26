define ([], function () {

    $_DO.update_charter_payment_new = function (e) {

        var form = w2ui ['charter_payment_new_form']

        var v = form.values ()
        
        var it = $('body').data ('data').item
        
        if (!v.begindate) die ('begindate', 'Укажите, пожалуйста, дату начала')

        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста, дату окончания')
        if (v.enddate < v.begindate) die ('enddate', 'Дата начала превышает дату окончания управления')
        
        v.uuid_charter = $_REQUEST.id

        query ({type: 'charter_payments', action: 'create', id: undefined}, {data: v}, function (data) {
        
            w2popup.close ()

            if (data.id) w2confirm ('Услуга зарегистрирована. Открыть её страницу в новой вкладке?').yes (function () {openTab ('/charter_payment/' + data.id)})
            
            var grid = w2ui ['charter_payments_grid']

            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_charter_object: "",
            begindate: dt_dmy (data.item.date_),
        }
        
        var root = data.charter_objects
                
        var a = []

        if (root.length == 1) {
            data.record.uuid_charter_object = root [0].id
        }
        else {
            a.push ({id: "", text: "Все объекты устава"})
        }
        
        $.each (root, function () {
            a.push (this)
        })
        
        data.objects = a
        
        done (data)

    }

})