define ([], function () {

    var form_name = 'citizen_compensation_payment_popup_form'

    $_DO.update_citizen_compensation_payment_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        v.uuid_cit_comp = $_REQUEST.id

        if (!v.paymentdate)    die ('paymentdate', 'Укажите, пожалуйста, дату выплаты')
        if (!v.paymenttype)    die ('paymenttype', 'Укажите, пожалуйста, тип выплаты')
        if (!v.paymentsum)     die('paymentsum', 'Укажите, пожалуйста, сумму выплаты')

        var tia = {type: 'citizen_compensation_payments'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['citizen_compensation_payments_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})