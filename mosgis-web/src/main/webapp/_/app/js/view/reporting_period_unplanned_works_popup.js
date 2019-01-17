define ([], function () {

    return function (data, view) {
    
        var name = 'reporting_period_unplanned_works_popup_form'
    
        function recalc () {
        
            var form = w2ui [name]
            
            var v = form.values ()
            
            var code_vc_nsi_56 = v.code_vc_nsi_56
            
            var o = {
                code_vc_nsi_57: code_vc_nsi_56 == 3,
                accidentreason: code_vc_nsi_56 == 3,
                organizationguid: code_vc_nsi_56 == 5,
                code_vc_nsi_3: code_vc_nsi_56 == 5 || (code_vc_nsi_56 == 3 && v.code_vc_nsi_57 > 1)
            }

            var sh = 0            
            for (id in o) {
                var h = o [id] ? 1 : 0
                sh += h
                $('#' + id).closest ('.w2ui-field').css ({display: h ? 'block' : 'none'})
            }            
            
            setTimeout (10, function () {
                form.refresh ()
            })
            
//darn (sh)            
            
//            darn ($('div[name=reporting_period_unplanned_works_popup_form]').height ())            
            
        
        }    

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                                                
                    {name: 'code_vc_nsi_56', type: 'hidden'},
                    {name: 'accidentreason', type: 'text'},
                    {name: 'amount', type: 'text'},
                    {name: 'code_vc_nsi_3', type: 'text', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_57', type: 'text', type: 'list', options: {items: data.vc_nsi_57.items}},
                    {name: 'comment_', type: 'text'},
                    {name: 'count', type: 'text'},
                    {name: 'organizationguid', type: 'text'},
                    {name: 'price', type: 'text'},
                    {name: 'uuid_org_work', type: 'list', options: {items: data.org_works.items}},
                ],
                
                onChange: function (e) {

                    if (e.target == "uuid_org_work") {
                        $('#unit').text (e.value_new.unit)
                        this.record.code_vc_nsi_56 = parseInt (e.value_new.code_vc_nsi_56)
                        e.done (recalc)
                        
//                        this.refresh ()
                    }
                    else if (e.target == "code_vc_nsi_57") {
                        e.done (recalc)
//                        this.refresh ()
                    }

                },
                                
            })
            
            recalc ()

       })

    }

})