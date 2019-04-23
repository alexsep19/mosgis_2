define ([], function () {

    var form_name = 'bank_account_rokr_popup_form'

    $_DO.update_bank_account_rokr_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        v.is_rokr = 1
        
        if (!v.opendate) die ('opendate', 'Укажите, пожалуйста, дату открытия счёта')

        if (!v.bikcredorg) die ('bikcredorg', 'Укажите, пожалуйста, БИК банка (филиала), в котором открыт счёт')

        if (!v.accountnumber) die ('accountnumber', 'Укажите, пожалуйста, номер лицевого счёта')

        if (!/^\d{20}$/.test (v.accountnumber)) die ('accountnumber', 'Номер лицевого счёта должен состоять ровно из 20 арабских цифр')

        
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

    $_DO.open_orgs_bank_account_rokr_popup = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {
            $('body').data('data', saved.data)
            $_SESSION.set('record', saved.record)
            use.block('bank_account_rokr_popup')
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            saved.record.uuid_cred_org = r.uuid
            saved.record.label_cred_org = r.label

            done()

        })

        use.block('voc_organizations_popup')

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record') || {
            opendate: dt_dmy (new Date ().toJSON ())
        }

        done (data)

    }

})