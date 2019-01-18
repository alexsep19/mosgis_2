define ([], function () {

    $_DO.update_supply_resource_contract_subject_quality_levels_popup = function (e) {

        var form = w2ui ['supply_resource_contract_subject_quality_level_form']

        var v = form.values ()

        if (!v.code_vc_nsi_276) die ('code_vc_nsi_276', 'Укажите, пожалуйста, наименование показателя качества')

        if ((v.indicatorvalue_from || v.indicatorvalue) && !v.code_vc_okei)
            die ('code_vc_okei', 'Укажите, пожалуйста, единицы измерения')

        v.indicatorvalue_is = v.indicatorvalue_is || 0

        $.each(['indicatorvalue', 'indicatorvalue_from', 'indicatorvalue_to'], function(){
            if (v[this] > 99999999999999)
                die(this, 'Укажите, пожалуйста, меньшее значение')
        })


        var tia = {type: 'supply_resource_contract_quality_levels'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['supply_resource_contract_subject_quality_levels_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record') || {}

        query(
           {type: 'supply_resource_contract_quality_levels', part: 'vocs', id: undefined}
            , {uuid_sr_ctr: data.item.uuid_sr_ctr}, function (d) {

            add_vocabularies(d, {used_vc_nsi_276: 1})

            d.vc_nsi_276 = d.vc_nsi_276.filter(function (i) {

                var i_code_239   = i['vc_nsi_239.code']
                var subj_code_239 = data.item.code_vc_nsi_239

                return i_code_239 == subj_code_239
                    && (!(i.id in d.used_vc_nsi_276)
                        || data.record.id && i.id == data.record.code_vc_nsi_276
                    )
            })

            add_vocabularies (d, {
                vc_nsi_276: 1,
                vc_okei: 1,
            })

            for (voc in d){data[voc] = d[voc]}

            data._can = data.record._can = {
                update: data.item._can.edit? true : false
            }

            done(data)
        })

    }

})