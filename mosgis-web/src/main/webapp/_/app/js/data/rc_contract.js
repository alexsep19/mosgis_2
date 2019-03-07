define([], function () {

    $_DO.choose_tab_rc_contract = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content('main', '');
            layout.lock('main', 'Загрузка...', true);
        }

        localStorage.setItem('rc_contract.active_tab', name)

        use.block(name)

    }

    return function (done) {

        query({type: 'rc_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies(data, data)

            query({type: 'rc_contracts'}, {}, function (d) {

                var it = data.item = d.item
                
                data.tb_bnk_accts = d.tb_bnk_accts

                data.bnk_accts_actual = data.tb_bnk_accts.map (function (i) {return {
                    id: i.uuid,
                    text: 'Р/сч. ' + i.accountnumber + ', ' + i ['bank.namep'] +
                        (i.uuid_org == it.uuid_org ? '' : ', владелец: ' + i ['org.label'])
                }})

                data.bnk_accts_set = !it.uuid_bnk_acct ? [] : [{
                    id: it.uuid_bnk_acct,
                    text: 'Р/сч. ' + it ['bank_acct.accountnumber'] + ', ' + it ['vc_bic.namep'] +
                        (it ['bank_acct.uuid_org'] == it.uuid_org ? '' : ', владелец: ' + it ['org_bank_acct.label'])
                }]

                it.last_annul = data.last_annul

                it._can = {cancel: 1}

                var is_locked = it.is_deleted || (it.uuid_org_customer != $_USER.uuid_org)

                if (!is_locked) {

                    switch (it.id_ctr_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }

                    switch (it.id_ctr_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                            it._can.approve = 1
                    }

                    it._can.update = it._can.edit
                    it._can.set_bank_acct = it.id_ctr_status > 11

                    switch (it.id_ctr_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 1
                    }

                    switch (it.id_ctr_status) {
                        case 40:
                            it._can.annul = 1
                            it._can.create_payment = 1
                    }

                }
                $('body').data('data', data)

                done(data)

            })
        })

    }

})