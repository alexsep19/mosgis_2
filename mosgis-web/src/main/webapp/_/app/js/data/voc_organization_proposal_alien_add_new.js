define ([], function () {

    var form_name = 'voc_organization_proposal_alien_add_new_form'

    $_DO.open_orgs_voc_organization_proposal_alien_add_new = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {

            $('body').data('data', saved.data)

            $_SESSION.set('record', saved.record)

            use.block('voc_organization_proposal_alien_add_new')
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

    $_DO.update_voc_organization_proposal_alien_add_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        v.id_type = "3";

        if (!v.fullname)
            die ('fullname', 'Укажите, пожалуйста, полное наименование')

        if (!v.shortname)
            die('shortname', 'Укажите, пожалуйста, сокращенное наименование')

        if (!/^\d{11}$/.test(v.nza))
            die('nza', 'Укажите, пожалуйста, номер записи об аккредитации (11 цифр)')

        if (!v.accreditationstartdate)
            die('accreditationstartdate', 'Укажите, пожалуйста, дату внесения в реестр аккредитованных')

        if (!/^(\d{10}|\d{12})$/.test(v.inn))
            die('inn', 'Укажите, пожалуйста ИНН(10 или 12 цифр)')

        if (!/^\d{9}$/.test(v.kpp))
            die('kpp', 'Укажите, пожалуйста КПП (9 цифр)')

        if (!v.fiashouseguid)
            die('fiashouseguid', 'Укажите, пожалуйста, Адрес регистрации (ФИАС)')

        if (!v.registrationcountry)
            die('registrationcountry', 'Укажите, пожалуйста, страну регистрации')

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