define ([], function () {

    $_DO.choose_tab_legal_act = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('legal_act.active_tab', name)

        use.block (name)

    }

    return function (done) {

        query ({type: 'legal_acts'}, {}, function (data) {

            query ({type: 'legal_acts', part: 'vocs', id: undefined}, {}, function (d) {

                add_vocabularies(d, d)

                for (k in d) data [k] = d [k]

                var it = data.item


                it._can = {cancel: 1}

                if (!it.is_deleted && it.uuid_org == $_USER.uuid_org) {

                    it._can.edit_values = 1

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
                            it._can.annul = 1
                    }
                    it._can.update = it._can.edit
                }

                $('body').data ('data', data)

                data.item.status_label = data.vc_gis_status[data.item.id_ctr_status]
                data.item.level_label = data.vc_legal_act_levels[data.item.level_]
                data.item.err_text = data.item['out_soap.err_text']

                done (data)
            })

        })

    }

})