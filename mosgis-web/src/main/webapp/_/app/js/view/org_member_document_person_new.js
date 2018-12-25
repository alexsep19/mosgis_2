define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_member_person_new_form',

                record: data.record,

                fields : [
                    {name: 'uuid_org', type: 'hidden'},
                    {name: 'uuid_person_member', type: 'list', options: {items: data.vc_persons}},
                    {name: 'participant', type: 'list', options: {items: data.vc_org_prtcps.items}},
                    {name: 'dt_from', type: 'date'},

                ],

                focus: 0,

                onRefresh: function (e) {e.done (function () {

                    clickOn ($('#label_person_member'), $_DO.open_persons_org_member_document_person_new)

                })},

                onChange: function (e) {

                    var form = this

                    if (e.target == "participant") {

                        e.done(function () {

                            var off = e.value_new.id == 35 // не член товарищества

                            var dt = form.get('dt_from')

                            dt.$el.prop('disabled', off)

                            if (off)
                                delete form.record ['dt_from']

                            form.refresh()

                        })

                    }

                },

            })

            if (!data.record.uuid_person_member) $('#label_person_member').click ()

       })

    }

})