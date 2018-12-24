define ([], function () {

    var form_name = 'property_document_house_org_new_form'

    $_DO.update_property_document_house_org_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        if (!v.uuid_house)        die('uuid_house', 'Вы забыли указать адрес')
        if (!v.uuid_org_owner)    die ('uuid_org_owner', 'Вы забыли указать собственника')
        if (!v.uuid_premise)      die ('uuid_premise', 'Вы забыли указать помещение')

        var p = parseFloat (v.prc)
        if (!(p > 0 && p <= 100)) die ('prc', 'Некорректно указан размер доли')

        if (v.dt && v.dt > new Date ().toISOString ()) die ('dt', 'Дата документа не может находиться в будущем')

        query ({type: 'property_documents', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Документ о праве собственности зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/property_document/' + data.id)})

            var grid = w2ui ['organization_member_property_documents_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

        data.vc_orgs = [{id: data.record.uuid_org_owner, text: data.record.label_org_owner}]

        delete data.record.label_org_owner

        done(data)

    }

})