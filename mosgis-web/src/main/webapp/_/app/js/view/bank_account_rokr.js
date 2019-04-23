define ([], function () {

    return function (data, view) {

        var it = data.item
        
        $('title').text ('Платежные реквизиты ' + it.label)

        if (it ['out_soap.err_text']) {

            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'

        }
        else if (it.id_ctr_status == 40) {

            if (!it.uuid_out_soap) {

                it.sync_label = 'Ожидание отправки в ГИС ЖКХ'

            }
            else if (!it.bankaccountguid) {

                it.sync_label = 'Ожидание ответа от ГИС ЖКХ'

            }

        }

        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 380,

                    tabs: {

                        tabs: [
                            {id: 'bank_account_rokr_common',     caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_bank_account_rokr

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'bank_account_rokr.active_tab')
            },

        });

        if (it.uuid_org) {
            clickOn ($('#lnk_org'), function () {openTab ('/voc_organization_legal/' + it.uuid_org)})
        }

    }

})