define ([], function () {

    var form_name = 'bank_account_rokr_common_form'

    $_DO.cancel_bank_account_rokr_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'bank_accounts'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            fix(it)

            $_F5 (data)

        })

    }

    $_DO.edit_bank_account_rokr_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }


    $_DO.update_bank_account_rokr_common = function (e) {

        var form = w2ui [form_name]

        var v = form.values()
        v.is_rokr = 1

        if (!v.opendate) die ('opendate', 'Укажите, пожалуйста, дату открытия счёта')

        if (!v.bikcredorg) die ('bikcredorg', 'Укажите, пожалуйста, БИК банка (филиала), в котором открыт счёт')

        if (!v.accountnumber) die ('accountnumber', 'Укажите, пожалуйста, номер лицевого счёта')

        if (!/^\d{20}$/.test (v.accountnumber)) die ('accountnumber', 'Номер лицевого счёта должен состоять ровно из 20 арабских цифр')


        if ($_USER.role.admin)
            v.uuid_org = $_REQUEST.id

        var tia = {type: 'bank_accounts', id: form.record.uuid}

        if (tia.id) {
            tia.action = 'update'
        } else {
            tia.action = 'create'
        }

        query(tia, {data: v}, reload_page)
    }

    $_DO.delete_bank_account_rokr_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'bank_accounts', action: 'delete'}, {}, reload_page)
    }

    $_DO.approve_bank_account_rokr_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'bank_accounts', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_bank_account_rokr_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'bank_accounts', action: 'alter'}, {data: {}}, reload_page)
    }

    $_DO.annul_bank_account_rokr_common = function (e) {
        if (!confirm ('Аннулировать эти данные в ГИС ЖКХ?')) return
        query ({type: 'bank_accounts', action: 'annul'}, {data: {}}, reload_page)
    }
    
    $_DO.terminate_bank_account_rokr_common = function (e) {
        use.block ('bank_account_rokr_terminate_popup')
    }
    
    $_DO.set_bank_acct_bank_account_rokr_common = function (e) {   
        use.block ('bank_acct_select_popup')
    }

    $_DO.choose_tab_bank_account_rokr_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('bank_account_rokr_common.active_tab', name)

        use.block (name)

    }

    $_DO.open_orgs_bank_account_rokr_common = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {
            f.refresh()
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            f.record.uuid_cred_org = r.uuid
            f.record.label_cred_org = r.label

            done()

        })

        use.block('voc_organizations_popup')

    }

    function fix(it) {

        if (it.uuid_cred_org) it.label_cred_org = it ['cred_org.label']

        it.err_text = it ['soap.err_text']

        var data = clone($('body').data('data'))

        it.status_label     = data.vc_gis_status [it.id_ctr_status]
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('bank_account_rokr_common.active_tab') || 'bank_account_rokr_common_log'

        var it = data.item

        fix (it)

        data.__read_only = it.__read_only = true

        done (data)

    }

})