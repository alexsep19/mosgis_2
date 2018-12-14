define ([], function () {

    var form_name = 'org_member_person_new_form'

    $_DO.update_org_member_document_person_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.uuid_person_member) die ('uuid_person_member', 'Вы забыли указать физлицо - член товарищества, кооператива')
        if (!v.participant) die ('participant', 'Вы забыли указать участие участие в совете, комиссии')

        query ({type: 'organization_members', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Документ о членстве зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/organization_member/' + data.id)})

            var grid = w2ui ['voc_organization_legal_members_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

        query ({type: 'organization_members', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies(d, d)

            data.vc_org_prtcps = d.vc_org_prtcps

            query ({type: 'vc_persons', id: undefined}, {uuid_org: $_REQUEST.id, offset: 0, limit: 1000000}, function (d) {

                data.vc_persons = d.root.map (function (i) {return {
                    id: i.id,
                    text: i.label
                }})

            })

            done (data)

        })

    }

})