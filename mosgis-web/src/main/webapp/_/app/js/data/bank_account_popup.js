define ([], function () {

    var form_name = 'bank_account_popup_form'

    $_DO.update_bank_account_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.accountnumber) die ('accountnumber', 'Укажите, пожалуйста, номер лицевого счёта')
        if (!/^\d{20}$/.test (v.accountnumber)) die ('accountnumber', 'Номер лицевого счёта должен состоять ровно из 20 арабских цифр')
        
        if (!v.bikcredorg) die ('bikcredorg', 'Укажите, пожалуйста, БИК банка (филиала), в котором открыт счёт')

        if (!v.opendate) die ('opendate', 'Укажите, пожалуйста, дату открытия счёта')

        if (v.closedate && v.closedate < v.opendate) die ('closedate', 'Дата закрытия не может быть ранее даты открытия')
        
        if ($_USER.role.admin) v.uuid_org = $_REQUEST.id

        var tia = {type: 'bank_accounts', id: form.record.uuid}
        
        if (tia.id) {
            tia.action = 'update'
        }
        else {
            tia.action = 'create'
        }

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['voc_organization_legal_bank_accounts_grid']
            grid.reload (grid.refresh)
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record') || {
            opendate: dt_dmy (new Date ().toJSON ())
        }

        done (data)

    }

})