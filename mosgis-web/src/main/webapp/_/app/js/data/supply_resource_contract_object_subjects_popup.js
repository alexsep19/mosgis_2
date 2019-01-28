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

        query ({type: 'supply_resource_contract_object_subjects', action: 'create', id: undefined}, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

        query ({type: 'supply_resource_contract_object_subjects', part: 'vocs', id: undefined}
	    , {uuid_sr_ctr: data.item['sr_ctr.uuid']}, function (d) {

            add_vocabularies (d, d)

            for (var k in d) {
                data[k] = d[k]
            }

            done (data)
        })

    }

})