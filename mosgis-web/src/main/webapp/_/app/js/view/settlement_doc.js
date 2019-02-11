define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text ('Расчеты по ДРСО ' + it['sr_ctr.label'])

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

                {type: 'main', size: 200,

                    tabs: {

                        tabs: [
                            {id: 'settlement_doc_common',     caption: 'Общие'},
                            {id: 'settlement_doc_common_log', caption: 'История изменений'}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_settlement_doc

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'settlement_doc.active_tab')
            },

        });

        clickOn ($('#lnk_sr_ctr'), function () {openTab ('/supply_resource_contract/' + it.uuid_sr_ctr)})
    }

})