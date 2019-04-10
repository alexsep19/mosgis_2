define ([], function () {

    var form_name = 'payment_new_form'

    $_DO.update_payment_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.orderdate) die('orderdate', 'Укажите дату внесения платы')
        if (!v.amount) die('amount', 'Укажите сумму')

        var ym = v.period.split (/-0?/)

        v.year = ym [0]
        v.month = ym [1]

        var dt = new Date ();

        if (v.year == dt.getFullYear () && v.month > (1 + dt.getMonth ())) die ('month', 'Этот период ещё не наступил')

        function done () {

            query ({type: 'payments', action: 'create', id: null}, {data: v}, function (data) {
                w2popup.close ()
                var grid = w2ui ['payments_grid']
                grid.reload (grid.refresh)
                w2confirm ('Платёж зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/payment/' + data.id)})
            })

        }

        var dt = v.year + '-' + String (v.month).padStart (2, '0') + '-01'

        query ({type: 'payments', id: null},

            {
              searchLogic: "AND",
              limit: 100,
              offset: 0,
              data: {uuid_account: v.uuid_account},
              search: [
                {field: "dt_period", type: "date", operator: "between", value: [dt, dt]},
                {field: "id_ctr_status", type: "enum", operator: "in", value: [{id: 10}]},
                {field: "is_deleted", type: "enum", operator: "in", value: [{id: "0"}]},
              ],
            }

            , function (d) {

                if (!d.root.length) return done ()
                w2popup.close ()
                w2confirm ('Такой платёжный документ уже зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/payment/' + d.root [0].id)})

            }

        )

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        var it = data.item

        var now = new Date ()

//        var dt_from = it ['ca.effectivedate'] || it ['ch.date_'] || it ['sr_ctr.effectivedate']
//
//        if (dt_from) {
//            dt_from = new Date (dt_from)
//            dt_from.setDate (1)
//        }
//        else {
            dt_from = new Date ()
            dt_from.setDate (1)
            dt_from.setMonth (0)
            dt_from.setFullYear (dt_from.getFullYear () - 3)
//        }

//        var dt_to = it ['ca.terminate'] || it ['ca.plandatecomptetion'] || it ['ch.terminate'] || it ['sr_ctr.terminate'] || it ['sr_ctr.completiondate']
//
//        if (dt_to) {
//            dt_to = new Date (dt_to)
//            if (dt_to > now) dt_to = new Date ()
//            dt_to.setDate (1)
//        }
//        else {
            dt_to = new Date ()
            dt_to.setDate (1)
//        }

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

        data.record = $_SESSION.delete('record') || {}

        data.record.period = periods [0].id

        done (data)

    }

})