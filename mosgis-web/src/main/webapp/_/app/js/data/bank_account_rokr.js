define([], function () {

    $_DO.choose_tab_bank_account_rokr = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content('main', '');
            layout.lock('main', 'Загрузка...', true);
        }

        localStorage.setItem('bank_account_rokr.active_tab', name)

        use.block(name)

    }

    return function (done) {

        query({type: 'bank_accounts'}, {}, function (data) {

            add_vocabularies(data, {
                vc_actions: 1,
                vc_gis_status: 1,
            })

            var it = data.item

            it.label = 'Р/сч. ' + it.accountnumber + ', ' + it ['bank.namep'] +
                ', владелец: ' + it ['org.label']

            it.last_annul = data.last_annul

            it._can = {cancel: 1}

            var is_locked = it.is_deleted || (it.uuid_org != $_USER.uuid_org)

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
                    case 94:
                        it._can.terminate = 1
                }

                switch (it.id_ctr_status) {
                    case 40:
                        it._can.create_payment = 1
                }

                switch (it.id_ctr_status) {
                    case 40:
                    case 100:
                    case 104:
                        it._can.annul = 1
                }
            }

            $('body').data('data', data)

            done(data)

        })

    }

})