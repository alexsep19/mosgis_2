define ([], function () {

    $_DO.choose_tab_supply_resource_contract = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract.active_tab', name)

        use.block (name)

    }

    return function (done) {

            query ({type: 'supply_resource_contracts'}, {}, function (data) {

                data.item.customer_label = data.item['org_customer.label'] || data.item['person_customer.label']
                
                add_vocabularies (data, {
                    vc_actions: 1,
                    vc_gis_status: 1,
                    vc_gis_ctr_dims: 1,
                    vc_nsi_58: 1,
                    vc_sr_ctr_file_types: 1
                })

                var it = data.item

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

                $('body').data ('data', data)

                done (data)

            })


    }

})