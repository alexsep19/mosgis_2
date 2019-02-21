define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text ('Коммунальный ресурс по адресу ' + it['building.label'] + ' ДРСО ' + it['sr_ctr.label'])

        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'supply_resource_contract_object_subject_common',   caption: 'Общие'}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_supply_resource_contract_object_subject

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'supply_resource_contract_object_subject.active_tab')
            },

        });

        if (it['sr_ctr.uuid']) {
            clickOn ($('#lnk_sr_ctr'), function () {openTab ('/supply_resource_contract/' + it['sr_ctr.uuid'])})
        }

        if (it['sr_ctr.uuid_org_customer']) {
            clickOn($('#lnk_customer'), function () {
                openTab('/voc_organization_legal/' + it['sr_ctr.uuid_org_customer'])
            })
        } else {
            clickOn($('#lnk_customer'), function () {
                openTab('/vc_person/' + it['sr_ctr.uuid_person_customer'])
            })
        }
        clickOn($('#lnk_house'), function () {
            openTab('/supply_resource_contract_object/' + it['uuid_sr_ctr_obj'])
        })
    }

})