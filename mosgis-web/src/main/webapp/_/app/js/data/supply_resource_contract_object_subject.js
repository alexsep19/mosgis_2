define ([], function () {

    $_DO.choose_tab_supply_resource_contract_object_subject = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_object_subject.active_tab', name)

        use.block (name)

    }

    return function (done) {

            query ({type: 'supply_resource_contract_object_subjects', part: 'vocs', id: undefined}, {}, function (d) {

                add_vocabularies (d, d)

                d.service2resource = {}

                $.each(d.vw_ms_r.items, function () {
                    d.service2resource[this.code_vc_nsi_3] = d.service2resource[this.code_vc_nsi_3] || {}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] =
                            d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] || []
                    var voc_okei = {id: this.code_vc_okei, text: d.vc_okei[this.code_vc_okei]}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239].push(voc_okei)
                })

                query ({type: 'supply_resource_contract_object_subjects'}, {}, function (data) {

                    var it = data.item

                    it._can = {
                        cancel: 1,
                        update: 1
                    }

                    var is_locked = it.is_deleted || (it['sr_ctr.uuid_org'] != $_USER.uuid_org)

                    if (!is_locked) {

                        switch (it['sr_ctr.id_ctr_status']) {
                            case 10:
                            case 14:
                                it._can.delete = 1
                        }

                        switch (it['sr_ctr.id_ctr_status']) {
                            case 10:
                            case 11:
                                it._can.edit = 1
                        }

                    }

                    for (voc in d) {
                        data[voc] = d[voc]
                    }

                    $('body').data ('data', data)

                    query({type: 'supply_resource_contract_subjects', id: undefined}
                        , {limit: 10000, offset: 0, data: {uuid_sr_ctr: it.uuid_sr_ctr}}
                        , function (d) {
                        var in_subj = d.tb_sr_ctr_subj.reduce(function (result, i, idx, array) {
                            result.code_vc_nsi_3[i.code_vc_nsi_3] = i
                            result.code_vc_nsi_239[i.code_vc_nsi_239] = i
                            return result
                        }, {code_vc_nsi_3: {}, code_vc_nsi_239: {}})

                        data.vc_nsi_3.items = data.vc_nsi_3.items.filter(function (i) {
                            return in_subj.code_vc_nsi_3[i.id]
                        })
                        data.vc_nsi_239.items = data.vc_nsi_239.items.filter(function (i) {
                            return in_subj.code_vc_nsi_239[i.id]
                        })

                        done(data)
                    })

                })
            })
    }

})