define ([], function () {

    return function (data, view) {

        var it = data.item
        
        it.service_type_label = data.vc_rc_ctr_service_types [it.id_service_type]

        $('title').text ('Договор РЦ ' + it.label)

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
                            {id: 'rc_contract_common',     caption: 'Общие'},
                            {id: 'rc_contract_objects',    caption: 'Объекты жилищного фонда', off: it.is_all_house}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_rc_contract

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'rc_contract.active_tab')
            },

        });

        if (it.uuid_org_customer) {
            clickOn ($('#lnk_customer'), function () {openTab ('/voc_organization_legal/' + it.uuid_org_customer)})
        }

    }

})