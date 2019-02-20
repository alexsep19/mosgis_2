define ([], function () {

    $_DO.update_supply_resource_contract_object_subjects_popup = function (e) {

        var form = w2ui ['supply_resource_contract_object_subjects_popup_form']

        var v = form.values ()

        var it = $('body').data ('data').item

        if (!v.startsupplydate)
            die ('startsupplydate', 'Укажите, пожалуйста, дату начала поставки ресурса')

        if (v.endsupplydate && v.endsupplydate < v.startsupplydate)
            die ('endsupplydate', 'Дата начала поставки ресурса превышает дату окончания поставки ресурса')

        v.uuid_sr_ctr = it['sr_ctr.uuid']
        v.uuid_sr_ctr_obj = $_REQUEST.id

        var tia = {type: 'supply_resource_contract_object_subjects', action: 'create', id: undefined}

        tia.id = form.record.id

        if (tia.id) {
            tia.action = 'update'
        }

        query (tia, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        var it = data.item

        data.record = $_SESSION.delete('record') || {}

        query ({type: 'supply_resource_contract_object_subjects', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies (d, d)

            for (var k in d) {
                data[k] = d[k]
            }

            query({type: 'supply_resource_contract_subjects', id: undefined}
                , {limit: 10000, offset: 0, data: {uuid_sr_ctr: it.uuid_sr_ctr}}
                , function (d) {
                    var in_subj = d.tb_sr_ctr_subj.reduce(function(result, i, idx, array){
                        result.code_vc_nsi_3[i.code_vc_nsi_3] = i
                        result.code_vc_nsi_239[i.code_vc_nsi_239] = i
                        return result
                    }, {code_vc_nsi_3: {}, code_vc_nsi_239: {}})

                    data.vc_nsi_3.items = data.vc_nsi_3.items.filter(function(i){
                        return in_subj.code_vc_nsi_3[i.id]
                    })
                    data.vc_nsi_239.items = data.vc_nsi_239.items.filter(function (i) {
                        return in_subj.code_vc_nsi_239[i.id]
                    })

                    done (data)
            })
        })

    }

})