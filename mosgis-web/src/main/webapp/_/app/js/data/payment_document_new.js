define ([], function () {

    var form_name = 'payment_document_new_form'

    $_DO.update_payment_document_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        var ym = v.period.split (/-0?/)
        
        v.year = ym [0]
        v.month = ym [1]

        var dt = new Date ();        

        if (v.year == dt.getFullYear () && v.month > (1 + dt.getMonth ())) die ('month', 'Этот период ещё не наступил')
        
        v.uuid_account = $_REQUEST.id
        
        function done () {
        
            query ({type: 'payment_documents', action: 'create', id: null}, {data: v}, function (data) {
                w2popup.close ()
                var grid = w2ui ['account_payment_documents_grid']
                grid.reload (grid.refresh)
                w2confirm ('Платёжный документ зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/payment_document/' + data.id)})
            })
            
        }
        
        var dt = v.year + '-' + String (v.month).padStart (2, '0') + '-01'
               
        query ({type: 'payment_documents', id: null}, 
        
            {
              searchLogic: "AND",
              limit: 100,
              offset: 0,
              data: {uuid_account: v.uuid_account},
              search: [
                {field: "dt_period", type: "date", operator: "between", value: [dt, dt]},
                {field: "id_ctr_status", type: "enum", operator: "in", value: [{id: 10}]},
                {field: "is_deleted", type: "enum", operator: "in", value: [{id: "0"}]},
                {field: "id_type", type: "enum", operator: "in", value: [{id: v.id_type}]},
              ],
            }        
        
            , function (d) {
            
                if (!d.root.length) return done ()
                w2popup.close ()
                w2confirm ('Такой платёжный документ уже зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/payment_document/' + d.root [0].id)})
            
            }
            
        )

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item       
        
        var now = new Date ()
        
        var dt_from = it ['ca.effectivedate'] || it ['ch.date_'] || it ['sr_ctr.effectivedate']
        
        if (dt_from) {
            dt_from = new Date (dt_from)
            dt_from.setDate (1)
        }
        else {
            dt_from = new Date ()
            dt_from.setDate (1)
            dt_from.setMonth (0)
            dt_from.setFullYear (dt_from.getFullYear () - 3)
        }
        
        var dt_to = it ['ca.terminate'] || it ['ca.plandatecomptetion'] || it ['ch.terminate'] || it ['sr_ctr.terminate'] || it ['sr_ctr.completiondate']

        if (dt_to) {
            dt_to = new Date (dt_to)
            if (dt_to > now) dt_to = new Date ()
            dt_to.setDate (1)
        }
        else {
            dt_to = new Date ()
            dt_to.setDate (1)
        }        
        
        var periods = []
        
        var dt = dt_to
        var ts_limit = dt_from.getTime ()
        
        while (periods.length < 36 && dt.getTime () > ts_limit) {
        
            periods.push ({
                id: dt.toJSON ().slice (0, 7),
                text: w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear ()
            })
            
            dt.setMonth (dt.getMonth () - 1)

        }        
        
        data.periods = periods

/*        
        var dt = new Date ();
        
        var yyyy = dt.getFullYear ()
        
        data.years = [
            {id: yyyy - 0, text: yyyy - 0},
            {id: yyyy - 1, text: yyyy - 1},
            {id: yyyy - 2, text: yyyy - 2},
        ]
        
        data.months = []; for (var i = 0; i < 12; i ++) data.months.push ({id: i + 1, text: w2utils.settings.fullmonths [i]})
*/
        data.record = {
            id_type: 0,
            period: periods [0].id
//            year: yyyy,
//            month: dt.getMonth () + 1,
        }

        done (data)

    }

})