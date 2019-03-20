define ([], function () {

    var form_name = 'payment_document_new_form'

    $_DO.update_payment_document_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        var dt = new Date ();        

        if (v.year == dt.getFullYear () && v.month > (1 + dt.getMonth ())) die ('month', 'Этот период ещё не наступил')
        
        v.uuid_account = $_REQUEST.id
        
        query ({type: 'payment_documents', action: 'create', id: null}, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['account_payment_documents_grid']
            grid.reload (grid.refresh)
            w2confirm ('Платёжный документ зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/payment_document/' + data.id)})
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var dt = new Date ();
        
        var yyyy = dt.getFullYear ()
        
        data.years = [
            {id: yyyy - 0, text: yyyy - 0},
            {id: yyyy - 1, text: yyyy - 1},
            {id: yyyy - 2, text: yyyy - 2},
        ]
        
        data.months = []; for (var i = 0; i < 12; i ++) data.months.push ({id: i + 1, text: w2utils.settings.fullmonths [i]})

        data.record = {
            id_type: 0,
            year: yyyy,
            month: dt.getMonth () + 1,
        }

        done (data)

    }

})