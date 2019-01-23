define ([], function () {

    var form_name = 'supply_resource_contract_object_subjects_popup_form'

    function recalc() {

	var form = w2ui [form_name]

	var v = form.values()
	var it = $('body').data('data').item

	var is_on = {
	    'input[name=is_heat_open]': ['3', '4'].indexOf(v.code_vc_nsi_239) != -1,
	    'input[name=is_heat_centralized]': ['3', '4'].indexOf(v.code_vc_nsi_239) != -1,
	    'input[name=volume]': it['sr_ctr.plannedvolumetype'] == 20,
	    'input[name=feedingmode]': it['sr_ctr.plannedvolumetype'] == 20,
	}

	var hidden = 0
	for (var s in is_on) {
	    $(s).closest('div.w2ui-field').toggle(is_on [s])
	    hidden = hidden + (is_on [s] ? 0 : 1)
	}


	var o = {
	    form: 293,
	    page: 215,
	    box: 311,
	    popup: 343,
	    'form-box': 293,
	}

	var $code_vc_nsi_239 = $('input[name=code_vc_nsi_239]')

	for (var k in o)
	    $code_vc_nsi_239.closest('.w2ui-' + k).height(o [k] - 31 * hidden)
    }

    function recalc_ms_change() {
	var data = $('body').data('data')
	var form = w2ui[form_name]
	var r = form.record
	var service = r.code_vc_nsi_3.id
	var resource = r.code_vc_nsi_239 ? r.code_vc_nsi_239.id : undefined

	form.get('code_vc_nsi_239').options.items = data.vc_nsi_239.items.filter(function (i) {
	    return data.service2resource[service] && data.service2resource[service][i.id]
	})

	var unit = data.service2resource[service] ? data.service2resource[service][resource] : undefined
	form.get('unit').options.items = unit || []
    }

    return function (data, view) {

        var service2resource = {}
        $.each (data.vw_ms_r, function(){
            service2resource[this.code_vc_nsi_3] = service2resource[this.code_vc_nsi_3] || {}
            service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] = 1
        })

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_239', type: 'list', options: {
			items: data.vc_nsi_239.items.filter(function (i) {
			    return data.service2resource[data.record.code_vc_nsi_3] && data.service2resource[data.record.code_vc_nsi_3][i.id]
			})
		    }},

                    {name: 'startsupplydate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.completiondate),
                    }},
                    {name: 'endsupplydate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.completiondate),
                    }},
		    {name: 'is_heat_open', type: 'list', options: {items: [
			{id: 0, text: 'Закрытое'},
			{id: 1, text: 'Открытое'},
		    ]}},
		    {name: 'is_heat_centralized', type: 'list', options: {items: [
			{id: 0, text: 'Нецентрализованная'},
			{id: 1, text: 'Централизованная'},
		    ]}},
		    {name: 'volume', type: 'float'},
		    {name: 'unit', type: 'list', options: {
			items: data.vc_nsi_239.items.filter(function (i) {
			    return data.service2resource[data.record.code_vc_nsi_3]? data.service2resource[data.record.code_vc_nsi_3][data.record.code_vc_nsi_239] : undefined || []
			})
		    }},

		    {name: 'feedingmode', type: 'text'},
                ],

                focus: data.record.id? -1 : 0,

		onRefresh : function(e) {
		    e.done(function(){
			recalc()
			recalc_ms_change()
		    })
		},

		onChange: function (e) {
		    if (e.target == 'code_vc_nsi_3' && e.value_new.id) {
			e.done(function () {
			    var r = w2ui[form_name].record
			    delete r.code_vc_nsi_239
			    delete r.unit
			    recalc_ms_change()
			    $().w2overlay(); // HACK: lost focus, hide dropdown on Enter
			    w2ui[form_name].refresh()
			})
		    }

		    if (e.target == 'code_vc_nsi_239' && e.value_new.id) {
			e.done(function () {
			    var r = w2ui[form_name].record
			    delete r.unit
			    recalc_ms_change()
			    $().w2overlay(); // HACK: lost focus, hide dropdown on Enter
			    w2ui[form_name].refresh()
			})
		    }
                },

            })

       })

    }

})