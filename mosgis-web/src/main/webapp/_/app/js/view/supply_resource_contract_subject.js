define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text ('Коммунальный ресурс по ДРС №' + it['sr_ctr.label'])

        if (it ['out_soap.err_text']) {

            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'

        }
        else if (it.id_ctr_status == 40) {

            if (!it.uuid_out_soap) {

                it.sync_label = 'Ожидание отправки в ГИС ЖКХ'

            }
            else if (!it.contractguid) {

                it.sync_label = 'Ожидание ответа от ГИС ЖКХ'

            }

        }

        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'supply_resource_contract_subject_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_supply_resource_contract_subject

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'supply_resource_contract_subject.active_tab')
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

    }

})