define ([], function () {

    var form_name = 'org_member_person_new_form'

    $_DO.update_citizen_compensation_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        v.uuid_org = $_USER.uuid_org

        if (!v.uuid_person)      die ('uuid_person', 'Вы забыли указать физлицо')
        if (!v.fiashouseguid)    die ('fiashouseguid', 'Вы забыли указать адрес')
        if (!v.registrationtype) die ('registrationtype', 'Вы забыли указать тип регистрации')

        query ({type: 'citizen_compensations', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id) w2confirm ('Гражданин зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/citizen_compensation/' + data.id)})

            use.block('citizen_compensations')

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

        query ({type: 'citizen_compensations', part: 'vocs', id: undefined}, {}, function (d) {

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