define ([], function () {

    var form_name = 'acknowledgment_common_form'

    $_DO.approve_acknowledgment_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'acknowledgments', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_acknowledgment_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'acknowledgments', action: 'alter'}, {}, reload_page)
    }

/*
    $_DO.annul_acknowledgment_common = function (e) {
        use.block('payment_annul_popup')
    }    
*/
    $_DO.delete_acknowledgment_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'acknowledgments', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_acknowledgment_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('acknowledgment_common.active_tab', name)

        use.block (name)

    }

    function fix (it) {

        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]
        
        it.accountnumber  = it ['acct.accountnumber']
        it.customer_label = it ['org_customer.label'] || it ['ind_customer.label']
        it.pay_amount     = it ['pay.amount']

        it.err_text       = it ['out_soap.err_text']

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('acknowledgment_common.active_tab') || 'acknowledgment_common_items'

        data.__read_only = 1

        var it = data.item

        fix (it)

        done (data)

    }

})