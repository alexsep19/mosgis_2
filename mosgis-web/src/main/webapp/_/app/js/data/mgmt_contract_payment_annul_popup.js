define ([], function () {

    $_DO.update_mgmt_contract_payment_annul_popup = function (e) {

        var f = w2ui ['mgmt_contract_payment_annul_popup_form']

        var v = f.values ()

        if (!v.reason) die ('reason', 'Укажите, пожалуйста, причину аннулирования')
        if (v.reason.length > 1024) die ('reason', 'Максимальная допустимая длина — 1024 символа')

        query ({type: 'contract_payments', action: 'annul'}, {data: v}, reload_page)

    }

    return function (done) {

        done (clone ($('body').data ('data')))

    }

})