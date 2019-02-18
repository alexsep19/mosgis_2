define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'interval_new_form',

                record: data.record,

                fields : [  
                
                    {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_239', type: 'list', options: {items: []}},

                    {name: 'startdateandtime', type: 'datetime'},
                    {name: 'enddateandtime', type: 'datetime'},
                    
                    {name: 'intervalreason', type: 'text'}
                ],

                onChange: function (e) {

                    if (e.target == "code_vc_nsi_3") {

                        var form = this

                        delete form.record.code_vc_nsi_239

                        var code_vc_nsi_3 = e.value_new.id

                        e.done(function () {
                            var f_resource = form.get('code_vc_nsi_239').options
                            f_resource.items = data.service2resource[code_vc_nsi_3]
                            if (f_resource.items.length == 1) {
                                f_resource.selected = f_resource.items[0]
                            }
                            form.refresh()
                        })
                    }
                }
            })                       

       })

    }

})