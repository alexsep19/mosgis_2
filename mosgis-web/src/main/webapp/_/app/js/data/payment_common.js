define ([], function () {

    var form_name = 'payment_common_form'
/*
    $_DO.approve_payment_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'payments', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_payment_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'payments', action: 'alter'}, {}, reload_page)
    }
*/
    $_DO.cancel_payment_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'payments'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            fix (it)

            w2ui ['passport_layout'].get ('main').tabs.enable ('payment_common_charge_info', 'payment_common_log')

            $_F5 (data)

        })

    }

    $_DO.edit_payment_common = function (e) {

        $_SESSION.set ('edit_payment_common', 1)

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        $_F5 (data)
    }

    $_DO.update_payment_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.orderdate) die('orderdate', 'Укажите дату внесения платы')
        if (!v.amount) die('amount', 'Укажите сумму')
        v.paymentpurpose = $('textarea').val () || ''

        if (v.paymentpurpose.length > 1000) die ('paymentpurpose', 'Назначение платежа не может содержать более 1000 символов')

        query ({type: 'payments', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_payment_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'payments', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_payment_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('payment_common.active_tab', name)

        use.block (name)

    }

    function fix (it) {

        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]

        it.sign = it.debtpreviousperiods > 0 ? -1 : 1

        it.debtpreviousperiods *= (- it.sign)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('payment_common.active_tab') || 'payment_common_log'

        data.__read_only = 1

        var it = data.item

        fix (it)

        done (data)

    }

})