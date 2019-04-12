define ([], function () {

    $_DO.choose_tab_premise_usage_tarif = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('premise_usage_tarif.active_tab', name)

        use.block (name)

    }

    return function (done) {

        query ({type: 'premise_usage_tarifs'}, {}, function (data) {

            query ({type: 'premise_usage_tarifs', part: 'vocs', id: undefined}, {}, function (d) {

                add_vocabularies(d, d)

                for (k in d) data [k] = d [k]

                var it = data.item


                it._can = {cancel: 1}

                var is_locked = it.is_deleted || (it.uuid_org != $_USER.uuid_org) || !$_USER.has_nsi_20(7, 8, 10)

                if (!is_locked) {

                    switch (it.id_ctr_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                            it._can.approve = 1
                    }

                    switch (it.id_ctr_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }

                    switch (it.id_ctr_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 1
                    }
                    switch (it.id_ctr_status) {
                        case 40:
                        case 104:
                            it._can.annul = 1
                    }
                    it._can.update = it._can.edit
                }

                $('body').data ('data', data)

                data.item.status_label = data.vc_gis_status[data.item.id_ctr_status]
                data.item.err_text = data.item['out_soap.err_text']

                done (data)
            })

        })

    }

})