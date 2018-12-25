define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_member_document_org_new_form',

                record: data.record,

                fields : [
                    {name: 'uuid_org', type: 'hidden'},
                    {name: 'uuid_org_member', type: 'hidden'},
                    {name: 'label_org_member', type: 'text'},
                    {name: 'participant', type: 'list', options: {items: data.vc_org_prtcps.items}},
                    {name: 'dt_from', type: 'date'},
                ],

                focus: 2,

                onRefresh: function (e) {e.done (function () {

                    clickOn ($('#label_org_member'), $_DO.open_orgs_org_member_document_org_new)

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

            if (!data.record.uuid_org_member) $('#label_org_member').click ()

       })

    }

})