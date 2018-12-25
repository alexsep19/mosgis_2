define ([], function () {

    $_DO.update_participant_status = function(it) {

        var prefix = it.participant == 35?
            'Информация о члене ревизионной комиссии, не являющегося членом '
            : 'Информация о члене '
        ;
        $('#participant_status').text(prefix + it['org_parent.label'])
    }

    return function (data, view) {

        var it = data.item

        $('title').text (it.label)

        fill (view, it, $('#body'))

        is_jur = it.uuid_org_member? 1 : 0

        $_DO.update_participant_status (data.item)

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'organization_member_common',   caption: is_jur? 'Юридическое лицо' : 'Физическое лицо'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_organization_member

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'organization_member.active_tab')
            },

        });

        if (it.uuid_person_member && (it ['person.uuid_org'] == $_USER.uuid_org || $_USER.role.admin)) {

            clickOn ($('#person_link'), function () {

                openTab ('/vc_person/' + it.uuid_person_member)

            })

        }

        if (it.uuid_org_member) {

            clickOn ($('#person_link'), function () {

                openTab ((is_jur ? '/voc_organization_legal/' : '/voc_organization_individual/') + it.uuid_org_member)

            })

        }

    }

})