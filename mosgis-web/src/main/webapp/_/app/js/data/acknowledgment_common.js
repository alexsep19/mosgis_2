define ([], function () {

    var form_name = 'acknowledgment_common_form'
/*
    $_DO.approve_acknowledgment_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'payments', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_acknowledgment_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'payments', action: 'alter'}, {}, reload_page)
    }

    $_DO.annul_acknowledgment_common = function (e) {
        use.block('payment_annul_popup')
    }
    
    $_DO.cancel_acknowledgment_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'payments'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            fix (it)

            w2ui ['passport_layout'].get ('main').tabs.enable ('acknowledgment_common_charge_info', 'acknowledgment_common_log')

            $_F5 (data)

        })

    }

    $_DO.edit_acknowledgment_common = function (e) {

        $_SESSION.set ('edit_acknowledgment_common', 1)

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        $_F5 (data)
    }

    $_DO.update_acknowledgment_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.orderdate) die('orderdate', 'Укажите дату внесения платы')
        if (!v.amount) die('amount', 'Укажите сумму')

        query ({type: 'payments', action: 'update'}, {data: v}, reload_page)

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