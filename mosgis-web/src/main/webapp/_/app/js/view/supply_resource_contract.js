define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text ('ДРС №' + it.contractnumber + ' от ' + dt_dmy (it.signingdate))

        if (it ['out_soap.err_text']) {

            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'

        }
        else if (it.id_ctr_status == 40) {

            if (!it['out_soap.uuid']) {

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
                            {id: 'supply_resource_contract_common',     caption: 'Общие'},
                            {id: 'supply_resource_contract_docs',       caption: 'Документы'},
                            {id: 'supply_resource_contract_subjects',   caption: 'Предмет договора'},
			    {id: 'supply_resource_contract_objects',    caption: 'Объекты жилищного фонда'},
			    {id: 'supply_resource_contract_temperature_charts', caption: 'Температурный график'
				, off: !data.is_on_tab_temperature
			    },
			    {id: 'supply_resource_contract_common_log', caption: 'История изменений'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_supply_resource_contract

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'supply_resource_contract.active_tab')
            },

        });

        if (it.uuid_org_customer) {
            clickOn ($('#lnk_customer'), function () {openTab ('/voc_organization_legal/' + it.uuid_org_customer)})
        }
        else {
            clickOn ($('#lnk_customer'), function () {openTab ('/vc_person/' + it.uuid_person_customer)})
        }

        clickOn ($('#lnk_org'), function () {openTab ('/voc_organization_legal/' + it.uuid_org)})

    }

})