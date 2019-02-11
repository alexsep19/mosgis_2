define ([], function () {

    $_DO.update_settlement_doc_payments_popup = function (e) {

        var form = w2ui ['settlement_doc_payments_popup_form']

        var v = form.values ()

        var it = $('body').data ('data').item

        if (!v.month) die('month', 'Укажите, пожалуйста, месяц отчетного периода')
        if (!v.year) die ('year', 'Укажите, пожалуйста, год отчетного периода')

        v.year = parseFloat(v.year)

        if (!(1920 <= v.year && v.year <= 2050))
            die('year', 'В поле "Год" должен быть указан год между 1920 и 2050')

        v.id_type  = $_USER.has_nsi_20(2)? 1 : 2;

        if (v.id_type == 1) {

            v.credited = parseFloat (v.credited)
            if (!(v.credited >= 0)) die ('credited', 'В поле "Начислено за период" должна быть указана неотрицательная денежная сумма')

            v.receipt = parseFloat(v.receipt)
            if (!(v.receipt >= 0)) die('receipt', 'В поле "Оплачено за период" должна быть указана неотрицательная денежная сумма')

            v.debts = parseFloat (v.debts)
            if (!(v.debts >= 0))
                die ('debts', 'В поле "Задолженность" должна быть указана положительная или нулевая денежная сумма')

            v.overpayment = parseFloat(v.overpayment)
            if (!(v.overpayment >= 0))
                die('overpayment', 'В поле "Переплата" должна быть указана положительная или нулевая денежная сумма')
        } else {
            delete v.credited
            delete v.receipt
            delete v.debts
            delete v.overpayment
        }

        v.paid = parseFloat (v.paid)
        if (!(v.paid >= 0)) die ('paid', 'В поле "Оплачено" должна быть указана неотрицательная денежная сумма')

        v.uuid_st_doc = $_REQUEST.id
        v.uuid_org_author = $_USER.uuid_org
        
        form.lock ()
        
        var tia = {type: 'settlement_doc_payments'}
        tia.action = (tia.id = form.record.uuid) ? 'update' : 'create'

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            use.block('settlement_doc_payments')
        })
        
    }
    function month(i) {
        return {id: new String(i), text: w2utils.settings.fullmonths [i - 1]}
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        var now = new Date()

        data.record = $_SESSION.delete ('record') || {
            year: now.getFullYear(),
            month: month(1 + now.getMonth())
        }

        data.record.debts = data.record.overpayment? data.record.overpayment : -data.record.debts

        data.months = []
        for (var i = 1; i <= 12; i ++) data.months.push(month(i))

        done (data)
    }

})