define ([], function () {

    return function (data, view) {
    
        var name = 'reporting_period_unplanned_works_popup_form'
    
        function recalc () {
        
            var form = w2ui [name]
            
            var v = form.values ()
            
            var code_vc_nsi_56 = v.code_vc_nsi_56
darn (v)
            var o = {
                code_vc_nsi_57: code_vc_nsi_56 == 3,
                accidentreason: code_vc_nsi_56 == 3,
                organizationguid: code_vc_nsi_56 == 5,
                code_vc_nsi_3: code_vc_nsi_56 == 5 || (code_vc_nsi_56 == 3 && v.code_vc_nsi_57 > 1)
            }
darn (o)
            var sh = 0            
            for (id in o) {
                var h = o [id] ? 1 : 0
                sh += h
                $('#' + id).closest ('.w2ui-field').css ({display: h ? 'block' : 'none'})
            }            
            
            setTimeout (10, function () {
                form.refresh ()
            })
            
            var h = 235 + 30 * sh
            
            $('div.page-0[data-block-name=reporting_period_unplanned_works_popup]').height (500)
            
            var $f = $('div.w2ui-form[data-block-name=reporting_period_unplanned_works_popup]')            
            $f.height (h)
            $('.w2ui-form-box', $f).height (h - 2)
            
            $('#w2ui-popup').height (h + 45)
        
        }    

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                                                
                    {name: 'code_vc_nsi_56', type: 'hidden'},
                    {name: 'accidentreason', type: 'text'},
                    {name: 'amount', type: 'float', options: {precision: 3}},
                    {name: 'code_vc_nsi_3', type: 'text', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'code_vc_nsi_57', type: 'text', type: 'list', options: {items: data.vc_nsi_57.items}},
                    {name: 'comment_', type: 'text'},
                    {name: 'count', type: 'int'},
                    {name: 'label_organizationguid', type: 'text'},
                    {name: 'organizationguid', type: 'hidden'},
                    {name: 'price', type: 'float', options: {precision: 4}},
                    {name: 'uuid_org_work', type: 'list', options: {items: data.org_works.items}},
                ],
                
                focus: -1,
                
                onChange: function (e) {

                    if (e.target == "uuid_org_work") {
                        $('#unit').text (e.value_new.unit)
                        this.record.code_vc_nsi_56 = parseInt (e.value_new.code_vc_nsi_56)
                        e.done (recalc)
                    }
                    else if (e.target == "code_vc_nsi_57") {
                        e.done (recalc)
                    }

                },
                
                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#label_organizationguid'), $_DO.open_orgs_reporting_period_unplanned_works_popup)
                                    
                })},
                
                onRender: function (e) {
                
                    e.done (function () {
                        recalc ()
                        this.refresh ()
                    })
                
                }
                                
            })
            
            recalc ()

       })

    }

})