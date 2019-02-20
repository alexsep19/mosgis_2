define ([], function () {

    $_DO.choose_tab_supply_resource_contract_object = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_object.active_tab', name)

        use.block (name)

    }

    return function (done) {

            query ({type: 'supply_resource_contract_objects', part: 'vocs', id: undefined}, {}, function (d) {

                add_vocabularies (d, d)

                d.service2resource = {}

                $.each(d.vw_ms_r.items, function () {
                    d.service2resource[this.code_vc_nsi_3] = d.service2resource[this.code_vc_nsi_3] || {}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] =
                        d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] || []
                    var voc_okei = {id: this.code_vc_okei, text: d.vc_okei[this.code_vc_okei]}
                    d.service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239].push(voc_okei)
                })

                query ({type: 'supply_resource_contract_objects'}, {}, function (data) {

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

                    if (!it['house.uuid']){
                        return done(data)
                    }

                    query({type: 'premises', id: undefined}, {data: {uuid_house: it['house.uuid'] || '00'}}, function (d) {

                        data.premises = d.vw_premises.map(function (i) {
                            return {
                                id: i.id,
                                text: i.label
                            }
                        })

                        $('body').data('data', data)

                        done(data)
                    })

                })
            })
    }

})