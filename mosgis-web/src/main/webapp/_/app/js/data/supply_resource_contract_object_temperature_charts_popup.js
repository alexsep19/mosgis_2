define ([], function () {

    $_DO.update_supply_resource_contract_object_temperature_charts_popup = function (e) {

        var form = w2ui ['supply_resource_contract_object_temperature_charts_popup_form']

        var v = form.values ()

        var it = $('body').data ('data').item

        if (!v.outsidetemperature)
            die ('outsidetemperature', 'Укажите, пожалуйста, температуру наружного воздуха')

        if (!v.flowlinetemperature)
            die ('flowlinetemperature', 'Укажите, пожалуйста, температуру теплоносителя в подающем трубопроводе')

        if (!v.oppositelinetemperature)
	    die('oppositelinetemperature', 'Укажите, пожалуйста, температуру теплоносителя в обратном трубопроводе')

        v.uuid_sr_ctr = it['sr_ctr.uuid']
	v.uuid_sr_ctr_obj = $_REQUEST.id

        var tia = {type: 'supply_resource_contract_object_temperature_charts', action: 'create', id: undefined}
	tia.id = form.record.id
	tia.action = tia.id ? 'update' : 'create'

        query (tia, {data: v}, function (data) {

            w2popup.close ()

            use.block('supply_resource_contract_object_temperature_charts')

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete('record') || {}

	done (data)

    }

})