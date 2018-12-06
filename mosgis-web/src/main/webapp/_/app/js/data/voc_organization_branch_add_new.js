define ([], function () {

    var form_name = 'voc_organization_branch_add_new_form'

    $_DO.open_orgs_voc_organization_branch_add_new = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {

            $('body').data('data', saved.data)

            $_SESSION.set('record', saved.record)

            use.block('voc_organization_branch_add_new')
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            if (r.id_type != 1) {
                alert('Выберите организацию с типом Юридическое лицо')
            } else {
                saved.record.uuid_org_parent = r.uuid
                saved.record.label_org_parent = r.label
                saved.record.ogrn = r.ogrn
                saved.record.inn = r.inn
            }

            done()

        })

        $_SESSION.set('voc_organization_popup.id_type', 1);

        use.block('voc_organizations_popup')

    }

    $_DO.update_voc_organization_branch_add_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.uuid_org_parent)
            die ('f', 'Укажите, пожалуйста, головную организацию')


        if (!v.fullname)
            die ('fullname', 'Укажите, пожалуйста, полное наименование')

        if (!v.shortname)
            die('shortname', 'Укажите, пожалуйста, сокращенное наименование')

        if (!v.stateregistrationdate)
            die('stateregistrationdate', 'Укажите, пожалуйста, дату государственной регистрации')

        if (!/^\d{13}$/.test(v.ogrn))
            die('ogrn', 'Укажите, пожалуйста ОГРН(13 цифр)')

        if (!/^\d{9}$/.test(v.kpp))
            die('kpp', 'Укажите, пожалуйста КПП (9 цифр)')

        if (!/^\d{1,5}$/.test(v.okopf))
            die('okopf', 'Укажите, пожалуйста ОКОПФ(1-5 цифр)')

        var tia = {type: 'voc_organization_proposals', action: 'create'}

        query (tia, {data: v}, reload_page)

    }

    return function (done) {

        var data = {}

        $('body').data('data', data)

        data.record = $_SESSION.delete ('record')

        if (!data.record.label_org_parent)
            data.record.label_org_parent = 'Выберите организацию'

        data.record.id_type = 2

        done (data)

    }

})