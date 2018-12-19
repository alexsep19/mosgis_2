define ([], function () {
    
    $_DO.update_agreement_payment_new = function (e) {

        var form = w2ui ['agreement_payment_new_form']

        var v = form.values ()

        var it = $('body').data ('data').item
        
        if (!v.datefrom) die ('datefrom', 'Укажите, пожалуйста, дату начала')

        if (!v.dateto) die ('dateto', 'Укажите, пожалуйста, дату окончания')
        if (v.dateto < v.datefrom) die ('dateto', 'Дата начала превышает дату окончания управления')
        
        v.uuid_ctr = $_REQUEST.id
        
        form.lock ()

        query ({type: 'agreement_payments', action: 'create', id: undefined}, {data: v}, function (data) {
//          w2confirm ('Услуга зарегистрирована. Открыть её страницу в новой вкладке?').yes (function () {openTab ('/charter_payment/' + uuid_charter_payment)})
            var grid = w2ui ['charter_payments_grid']
            grid.reload (grid.refresh)                                           
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_ctr: "",
            begindate: dt_dmy (data.item.date_),
        }
        
        done (data)

    }

})