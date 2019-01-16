define ([], function () {

    return function (data, view) {

        var f = 'supply_resource_contract_subjects_new_form'
        var service2resource = {}
        $.each (data.vw_ms_r, function(){
            service2resource[this.code_vc_nsi_3] = service2resource[this.code_vc_nsi_3] || {}
            service2resource[this.code_vc_nsi_3][this.code_vc_nsi_239] = 1
        })

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: f,

                record: data.record,

                fields : [
                    {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_239', type: 'list', options: {items: data.vc_nsi_239.items}},

                    {name: 'startsupplydate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.completiondate),
                    }},
                    {name: 'endsupplydate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.effectivedate),
                        end:      dt_dmy (data.item.completiondate),
                    }}
                ],

                focus: 0,

                onChange: function (e) {
                    if (e.target == "code_vc_nsi_3") {

                        delete w2ui[f].record.code_vc_nsi_239

                        var service = e.value_new.id

                        e.done(function(){
                            w2ui[f].get('code_vc_nsi_239').options.items = data.vc_nsi_239.items.filter(function(i){
                                return service2resource[service] && service2resource[service][i.id]
                            })
                            w2ui[f].refresh()
                        })
                    }
                },

            })

       })

    }

})