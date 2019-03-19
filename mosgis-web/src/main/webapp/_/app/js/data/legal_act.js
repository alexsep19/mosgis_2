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

                data.item._can = {
                    edit: $_USER.has_nsi_20(7, 10),
                    approve: 0,
                    update: 1,
                    cancel: 1,
                }

                data.item._can.delete = data.item._can.edit

                if (!data.item.is_deleted) {

                    switch (data.item.id_ctr_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 0
                    }

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