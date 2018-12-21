define ([], function () {

    var form_name = 'org_member_document_org_new_form'

    $_DO.open_orgs_org_member_document_org_new = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }

        function done () {
            $('body').data ('data', saved.data)
            $_SESSION.set ('record', saved.record)
            use.block ('org_member_document_org_new')
        }

        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            saved.record.uuid_org_member = r.uuid
            saved.record.label_org_member = r.label

            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_org_member_document_org_new = function (e) {

        var form = w2ui [form_name]

        var r = form.record
        var v = form.values ()

        if (!(v.uuid_org_member = r.uuid_org_member)) die ('label_org_member', 'Вы забыли указать члена товарищества, кооператива')
        if (!v.participant) die ('participant', 'Вы забыли указать участие в совете, комиссии')

        query ({type: 'organization_members', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Документ о праве собственности зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/organization_member/' + data.id)})

            var grid = w2ui ['voc_organization_legal_members_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone($('body').data('data'))

        data.record = $_SESSION.delete('record') || {}

        query({type: 'organization_members', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies(d, d)

            data.vc_org_prtcps = d.vc_org_prtcps

            query({type: 'vc_persons', id: undefined}, {uuid_org: $_REQUEST.id, offset: 0, limit: 1000000}, function (d) {

                data.vc_persons = d.root.map(function (i) {
                    return {
                        id: i.id,
                        text: i.label
                    }
                })

            })

            done(data)

        })

    }

})