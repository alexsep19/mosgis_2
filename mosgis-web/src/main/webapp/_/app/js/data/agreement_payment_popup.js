define ([], function () {
    
    $_DO.update_agreement_payment_popup = function (e) {

        var form = w2ui ['agreement_payment_popup_form']

        var v = form.values ()

        var it = $('body').data ('data').item
        
        if (!v.datefrom) die ('datefrom', 'Укажите, пожалуйста, дату начала периода оплаты')
        if (!v.dateto) die ('dateto', 'Укажите, пожалуйста, дату окончания периода оплаты')
        if (v.dateto < v.datefrom) die ('dateto', 'Дата начала превышает дату окончания периода оплаты')

        if (v.datefrom < it.startdate.substr (0, 10)) die ('datefrom', 'Дата начала периода оплаты не может быть ранее даты вступления договора в силу')
        if (v.dateto > it.enddate.substr (0, 10)) die ('dateto', 'Дата окончания периода оплаты не может быть позже даты окончания действия договора')
        
        v.bill = parseFloat (v.bill)
        if (!(v.bill >= 0)) die ('bill', 'В поле "Начислено за период" должна быть указана неотрицательная денежная сумма')

        v.debt = parseFloat (v.debt)
        if (!((v.debt >= 0) || (v.debt < 0))) die ('debt', 'В поле "Задолженность" должна быть указана положительная, отрицательная или нулевая денежная сумма')

        v.paid = parseFloat (v.paid)
        if (!(v.paid >= 0)) die ('paid', 'В поле "Оплачено за период" должна быть указана неотрицательная денежная сумма')

        v.uuid_ctr = $_REQUEST.id
        
        form.lock ()
        
        var tia = {type: 'agreement_payments'}        
        tia.action = (tia.id = form.record.uuid) ? 'update' : 'create'

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['public_property_contract_agreement_payments_grid']
            grid.reload (grid.refresh)
        })
        
    }

    return function (done) {

        var data = clone ($('body').data ('data'))        
        data.record = $_SESSION.delete ('record')        
        done (data)

    }

})