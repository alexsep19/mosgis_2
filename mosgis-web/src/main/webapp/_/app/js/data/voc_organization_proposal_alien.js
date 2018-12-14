define ([], function () {

    var form_name = 'voc_organization_proposal_alien_form'

    $_DO.cancel_voc_organization_proposal_alien = function (e) {

        if (!confirm('Отменить несохранённые правки?'))
            return

        var data = w2ui [form_name].record

        query({type: 'voc_organization_proposals'}, {}, function (data) {

            data.__read_only = true

            $_F5(data)

        })

    }

    $_DO.edit_voc_organization_proposal_alien = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10)
//            die('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5(data)

    }

    $_DO.update_voc_organization_proposal_alien = function (e) {

        if (!confirm('Сохранить изменения?'))
            return

        var f = w2ui [form_name]

        var v = f.values()

        v.id_type = "3"

        if (!v.fullname)
            die('fullname', 'Укажите, пожалуйста, полное наименование')

        if (!v.shortname)
            die('shortname', 'Укажите, пожалуйста, сокращенное наименование')

        if (!/^\d{11}$/.test(v.nza))
            die('nza', 'Укажите, пожалуйста, номер записи об аккредитации (11 цифр)')

        if (!v.accreditationstartdate)
            die('accreditationstartdate', 'Укажите, пожалуйста, дату внесения в реестр аккредитованных')

        if (!valid_inn(v.inn))
            die('inn', 'Укажите, пожалуйста ИНН(10 или 12 цифр)')

        if (!valid_kpp(v.kpp))
            die('kpp', 'Укажите, пожалуйста КПП (9 цифр)')

        if (!v.fiashouseguid)
            die('fiashouseguid', 'Укажите, пожалуйста, Адрес регистрации (ФИАС)')

        if (!v.registrationcountry)
            die('registrationcountry', 'Укажите, пожалуйста, страну регистрации')

        var r = f.record;

        return query({type: 'voc_organization_proposals', id: r.uuid, action: 'update'}, {data: v}, reload_page)
    }

    $_DO.delete_voc_organization_proposal_alien = function (e) {
        if (!confirm('Удалить эту запись, Вы уверены?'))
            return
        query({type: 'voc_organization_proposals', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_voc_organization_proposal_alien = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('voc_organization_proposal_alien.active_tab', name)

        use.block (name)
    }

    return function (done) {

        query ({type: 'voc_organization_proposals'}, {}, function (data) {

            $('body').data ('data', data)

            done(data)

        })

    }

})