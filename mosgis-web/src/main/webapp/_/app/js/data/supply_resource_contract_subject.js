define ([], function () {

    $_DO.choose_tab_supply_resource_contract_subject = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_subject.active_tab', name)

        use.block (name)

    }

    return function (done) {

            query ({type: 'supply_resource_contract_subjects', part: 'vocs', id: undefined}, {}, function (d) {

                add_vocabularies (d, d)

                d.service2resource = {}

                $.each(d.vw_ms_r.items, function () {
                    d.service2resource[this.code_vc_nsi_3] = d.service2resource[this.code_vc_nsi_3] || {}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] =
                            d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] || []
                    var voc_okei = {id: this.code_vc_okei, text: d.vc_okei[this.code_vc_okei]}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239].push(voc_okei)
                })

                query ({type: 'supply_resource_contract_subjects'}, {}, function (data) {

                    var it = data.item

                    it._can = {
                        cancel: 1,
                        update: 1,
                        delete: it['sr_ctr.uuid_org'] == $_USER.uuid_org,
                        edit  : it['sr_ctr.uuid_org'] == $_USER.uuid_org,
                    }

                    for (voc in d) {
                        data[voc] = d[voc]
                    }

                    $('body').data ('data', data)

                    done (data)

                })
            })
    }

})